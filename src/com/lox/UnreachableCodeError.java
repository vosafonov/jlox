package com.lox;

public class UnreachableCodeError extends RuntimeError {
    UnreachableCodeError(Token token)
    {
        super(token, "Internal Error, unreachable code");
    }
}

