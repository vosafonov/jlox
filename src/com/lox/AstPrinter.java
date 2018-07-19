package com.lox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    //
    // Stmt.Visitor
    //
    String print(List<Stmt> statements)
    {
        StringBuilder builder = new StringBuilder();

        for (Stmt statement : statements) {
            builder.append(statement.accept(this));
            builder.append('\n');
        }

        return builder.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt)
    {
        return parenthesizeStmts("block", stmt.statements);
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt)
    {
        return parenthesizeFuncStmts("class def '" + stmt.name.lexeme + "' with functions", stmt.methods);
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt)
    {
        return parenthesize("stmt_expr", stmt.expression);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt)
    {
        String header = "function def '" + stmt.name.lexeme + "' with params " + parenthesize(stmt.parameters) + " and body";
        return parenthesizeStmts(header, stmt.body);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt)
    {
        String condition = parenthesize("condition", stmt.condition);
        String thenBranch = parenthesizeStmts("then", stmt.thenBranch);
        String elseBranch = "";
        if (stmt.elseBranch != null) {
            elseBranch = parenthesizeStmts("else", stmt.elseBranch);
        }

        return parenthesizeStmts("stmt_if " + condition + " " + thenBranch + " " + elseBranch);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt)
    {
        return parenthesize("stmt_print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt)
    {
        if (stmt.value != null) {
            return parenthesize("stmt_return", stmt.value);
        } else {
            return "stmt_return";
        }
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt)
    {
        String condition = parenthesize("condition", stmt.condition);
        String body = parenthesizeStmts("body", stmt.body);

        return parenthesizeStmts("stmt_while " + condition + " " + body);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt)
    {
        return parenthesize("stmt_var '" + stmt.name.lexeme + "'", stmt.initializer);
    }

    //
    // Expr.Visitor
    //
    String print(Expr expr)
    {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr)
    {
        return parenthesize("assign '" + expr.name.lexeme + "'", expr.value);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr)
    {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr)
    {
        return parenthesize("call " + expr.callee.accept(this) + " with ", expr.arguments);
    }

    @Override
    public String visitGetExpr(Expr.Get expr)
    {
        return parenthesize("get property '" + expr.name.lexeme + "' from object ", expr.object);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr)
    {
        return parenthesize("group", expr.expressions);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr)
    {
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr)
    {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        String obj = parenthesize("", expr.object);
        return parenthesize("set property '" + expr.name.lexeme + "' from object " + obj + " to ", expr.value);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr)
    {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr)
    {
        return parenthesize("variable '" + expr.name.lexeme + "'");
    }

    // TODO: упростить методы parenthesize
    private String parenthesize(List<Token> tokens)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        boolean first = true;
        for (Token token : tokens) {
            if (!first) {
                builder.append(" ");
            }
            first = false;
            builder.append(token.lexeme);
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String name, List<Expr> exprs)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String name, Expr... exprs)
    {
        List<Expr> args = new ArrayList<>();
        Collections.addAll(args, exprs);
        return parenthesize(name, args);
    }

    private String parenthesizeStmts(String name, List<Stmt> stmts)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Stmt stmt : stmts) {
            builder.append(" ");
            builder.append(stmt.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesizeStmts(String name, Stmt... stmts)
    {
        List<Stmt> args = new ArrayList<>();
        Collections.addAll(args, stmts);
        return parenthesizeStmts(name, args);
    }

    private String parenthesizeFuncStmts(String name, List<Stmt.Function> stmts)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Stmt stmt : stmts) {
            builder.append(" ");
            builder.append(stmt.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesizeFuncStmts(String name, Stmt.Function... stmts)
    {
        List<Stmt.Function> args = new ArrayList<>();
        Collections.addAll(args, stmts);
        return parenthesizeFuncStmts(name, args);
    }
}
