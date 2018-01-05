package com.lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expr)
    {
        try {
            Object value = evulation(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

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
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evulation(expr.left);
        Object right = evulation(expr.right);

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
                return (double)left / (double)right;
            case STAR:
                ensureNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        throw new UnreachableCodeError(expr.operator);
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
        return evulation(expr.expressions);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evulation(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                ensureNumberOperand(expr.operator, right);
                return -(double)right;
        }

        throw new UnreachableCodeError(expr.operator);
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

    private Object evulation(List<Expr> exprs)
    {
        Object lastResult = null;

        for (Expr expr : exprs) {
            lastResult = expr.accept(this);
        }

        return lastResult;
    }

    private Object evulation(Expr expr)
    {
        return expr.accept(this);
    }
}
