package com.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    LoxFunction(Stmt.Function declaration, Environment closure)
    {
        this.declaration = declaration;
        this.closure = closure;
    }

    LoxFunction bind(LoxInstance instance)
    {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env);
    }

    @Override
    public int arity()
    {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments)
    {
        Environment env = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); ++i) {
            env.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, env);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "<fn " + declaration.name.lexeme + ">";
    }

    private final Stmt.Function declaration;
    private final Environment closure;
}
