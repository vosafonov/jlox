package com.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
    }

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        final List<Stmt> statements;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class Class extends Stmt {
        Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Function extends Stmt {
        Function(Token name, List<Token> parameters, List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        final Token name;
        final List<Token> parameters;
        final List<Stmt> body;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    static class Return extends Stmt {
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        final Token keyword;
        final Expr value;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        final Token name;
        final Expr initializer;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        final Expr condition;
        final Stmt body;

        <R> R accept(Visitor <R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
