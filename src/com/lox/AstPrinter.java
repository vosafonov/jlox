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
    public String visitExpressionStmt(Stmt.Expression stmt)
    {
        return parenthesize("stmt_expr", stmt.expression);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt)
    {
        return parenthesize("stmt_print", stmt.expression);
    }

    //
    // Expr.Visitor
    //
    String print(Expr expr)
    {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr)
    {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
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
    public String visitUnaryExpr(Expr.Unary expr)
    {
        return parenthesize(expr.operator.lexeme, expr.right);
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
}
