package com.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expr left;
        final Token operator;
        final Expr right;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Grouping extends Expr {
        Grouping(List<Expr> expressions) {
            this.expressions = expressions;
        }

        final List<Expr> expressions;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        final Object value;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        final Token operator;
        final Expr right;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
