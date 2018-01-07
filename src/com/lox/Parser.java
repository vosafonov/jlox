package com.lox;

import java.util.ArrayList;
import java.util.List;
import static com.lox.TokenType.*;

//
// program   → statement* EOF ;
//
// statement → exprStmt
//           | printStmt ;
//
// exprStmt  → expression ";" ;
// printStmt → "print" expression ";" ;
//
// expression     → equality ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
// addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
// multiplication → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary
//                | primary ;
// primary        → NUMBER | STRING | "false" | "true" | "nil"
//                | "(" expression ("," expression)* ")" ;
//
class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    List<Stmt> parse()
    {
        try {
            List<Stmt> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(statement());
            }
            return statements;
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression()
    {
        return equlity();
    }

    private Stmt statement()
    {
        if (matchAny(PRINT)) {
            return printStatement();
        }

        return expressionStatement();
    }

    private Stmt printStatement()
    {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement()
    {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr equlity()
    {
        Expr expr = comparison();

        while (matchAny(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison()
    {
        Expr expr = addition();

        while (matchAny(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition()
    {
        Expr expr = multiplication();

        while (matchAny(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication()
    {
        Expr expr = unary();

        while (matchAny(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary()
    {
        if (matchAny(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary()
    {
        if (matchAny(FALSE)) {
            return new Expr.Literal(false);
        }
        if (matchAny(TRUE)) {
            return new Expr.Literal(true);
        }
        if (matchAny(NIL)) {
            return new Expr.Literal(null);
        }

        if (matchAny(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        List<Expr> exprs = new ArrayList<>();
        if (matchAny(LEFT_PAREN)) {
            exprs.add(expression());

            while (matchAny(COMMA)) {
                exprs.add(expression());
            }
            consume(RIGHT_PAREN, "Expect ')' after expression.");

            return new Expr.Grouping(exprs);
        }

        throw error(peek(), "Expect expression.");
    }


    private boolean matchAny(TokenType... types)
    {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message)
    {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type)
    {
        return !isAtEnd() && peek().type == type;
    }

    private Token advance()
    {
        if (!isAtEnd()) {
            ++current;
        }
        return previous();
    }

    private boolean isAtEnd()
    {
        return peek().type == EOF;
    }

    private Token peek()
    {
        return tokens.get(current);
    }

    private Token previous()
    {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message)
    {
        Lox.error(token, message);
        return new ParseError();
    }
}
