package com.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    LoxFunction(Stmt.Function declaration)
    {
        this.declaration = declaration;
    }

    @Override
    public int arity()
    {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments)
    {
        Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.parameters.size(); ++i) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        interpreter.executeBlock(declaration.body, environment);
        return null;
    }

    @Override
    public String toString()
    {
        return "<fn " + declaration.name.lexeme + ">";
    }

    private final Stmt.Function declaration;
}
