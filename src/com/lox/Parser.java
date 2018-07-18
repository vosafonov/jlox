package com.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.lox.TokenType.*;

//
// program     → declaration* eof ;
//
// declaration → classDecl
//             | funDecl
//             | varDecl
//             | statement ;
// classDecl → "class" IDENTIFIER "{" function* "}" ;
// funDecl → "fun" function ;
// function → IDENTIFIER "(" parameters? ")" block ;
// parameters → IDENTIFIER ( "," IDENTIFIER )* ;
// varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
//
// statement → exprStmt
//           | forStmt
//           | ifStmt
//           | printStmt
//           | returnStmt
//           | whileStmt
//           | block ;
//
// exprStmt  → expression ";" ;
// forStmt   → "for" "(" ( varDecl | exprStmt | ";" )
//                       expression? ";"
//                       expression? ")" statement ;
//
// ifStmt     → "if" "(" expression ")" statement ( "else" statement )? ;
// printStmt  → "print" expression ";" ;
// returnStmt → "return" expression? ";" ;
// whileStmt  → "while" "(" expression ")" statement ;
// block      →  "{" declaration* "}"
//
// expression     → assignment ;
//
//                на самом деле 'identifier' парсится как 'equality',
//                но с дополнительной проверкой
// assignment     → identifier "=" assignment
//                | logic_or ;
// logic_or       → logic_and ( "or" logic_and )* ;
// logic_and      → equality ( "and" equality )* ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
// addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
// multiplication → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary
//                | call ;
// primary        → NUMBER | STRING | "false" | "true" | "nil"
//                | "(" expression ( "," expression)* ")"
//                | IDENTIFIER ;
// call           → primary ( "(" arguments? ")" )* ;
// arguments      → expression ( "," expression )* ;
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
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            } catch (ParseError error) {
                synchronize();
            }
        }
        return statements;
    }

    private Stmt declaration()
    {
        if (matchAny(CLASS)) {
            return classDeclaration();
        }
        if (matchAny(FUN)) {
            return function("function");
        }
        if (matchAny(VAR)) {
            return varDeclaration();
        }
        return statement();
    }

    private Stmt classDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect class name.");

        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, methods);
    }

    private Stmt varDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (matchAny(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement()
    {
        if (matchAny(FOR)) {
            return forStatement();
        }
        if (matchAny(IF)) {
            return ifStatement();
        }
        if (matchAny(PRINT)) {
            return printStatement();
        }
        if (matchAny(RETURN)) {
            return returnStatement();
        }
        if (matchAny(WHILE)) {
            return whileStatement();
        }
        if (matchAny(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt forStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (matchAny(SEMICOLON)) {
            initializer = null;
        } else if (matchAny(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after 'for' clauses.");

        Stmt body = statement();

        if (increment !=  null) {
            body = new Stmt.Block(Arrays.asList(
                body,
                new Stmt.Expression(increment)));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt ifStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (matchAny(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement()
    {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement()
    {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'while' condition.");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private List<Stmt> block()
    {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement()
    {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind)
    {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 8 ) {
                    error(peek(), "Cannot have more than 8 parameters.");
                }

                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (matchAny(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Expr expression()
    {
        return assignment();
    }

    private Expr assignment()
    {
        Expr expr = logic_or();

        if (matchAny(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Expr.Variable varExpr = (Expr.Variable)expr;
                Token name = varExpr.name;
                return new Expr.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logic_or()
    {
        Expr expr = logic_and();

        while (matchAny(OR)) {
            Token operator = previous();
            Expr right = logic_and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr logic_and()
    {
        Expr expr = equlity();

        while (matchAny(AND)) {
            Token operator = previous();
            Expr right = equlity();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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

        return call();
    }

    private Expr call()
    {
        Expr expr = primary();

        while (matchAny(LEFT_PAREN)) {
            expr = finishCall(expr);
        }

        return expr;
    }

    private Expr finishCall(Expr callee)
    {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 8) {
                    error(peek(), "Cannot have more than 8 arguments.");
                }
                arguments.add(expression());
            } while (matchAny(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
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

        if (matchAny(IDENTIFIER)) {
            return new Expr.Variable(previous());
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

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
