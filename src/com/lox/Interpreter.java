package com.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    Interpreter()
    {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity()
            {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments)
            {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString()
            {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Stmt> statements)
    {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    //
    // Stmt.Visitor
    //
    @Override
    public Void visitBlockStmt(Stmt.Block stmt)
    {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt)
    {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt)
    {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt)
    {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt)
    {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new Return(value);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt)
    {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt)
    {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    private void execute(Stmt stmt)
    {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth)
    {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment)
    {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    //
    // Expr.Visitor
    //
    private String stringify(Object object)
    {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            // Убираем дробную часть, если она равна нулю.
            String text = object.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value = evaluate(expr.value);

        Integer depth = locals.get(expr);
        if (depth != null) {
            environment.assignAt(depth, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

            case GREATER:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;

            case MINUS:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                ensureNumberOperands(expr.operator, left, right);
                if ((double)right == 0) {
                    throw new RuntimeError(expr.operator, "Number division by zero.");
                }
                return (double)left / (double)right;
            case STAR:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        throw new UnreachableCodeError(expr.operator);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr)
    {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        LoxCallable function = (LoxCallable)callee;
        if (function.arity() != arguments.size()) {
            throw new RuntimeError(expr.paren, "Expected " +
                function.arity() + " arguments but got " +
                arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    private boolean isEqual(Object left, Object right)
    {
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
        return evaluate(expr.expressions);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr)
    {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (expr.operator.type == TokenType.AND) {
            if (!isTruthy(left)) return left;
        } else {
            throw new UnreachableCodeError(expr.operator);
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                ensureNumberOperand(expr.operator, right);
                return -(double)right;
        }

        throw new UnreachableCodeError(expr.operator);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr)
    {
        Integer depth = locals.get(expr);
        if (depth != null) {
            return environment.getAt(depth, name);
        } else {
            return globals.get(name);
        }
    }

    private void ensureNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void ensureNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operands must be a numbers.");
    }

    private boolean isTruthy(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean)object;
        }
        return true;
    }

    private Object evaluate(List<Expr> exprs)
    {
        Object lastResult = null;

        for (Expr expr : exprs) {
            lastResult = expr.accept(this);
        }

        return lastResult;
    }

    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();
}
