package com.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    Environment()
    {
        enclosing = null;
    }

    Environment(Environment enclosing)
    {
        this.enclosing = enclosing;
    }

    void define(String name, Object value)
    {
        values.put(name, value);
    }

    private Environment ancestor(int depth)
    {
        Environment env = this;
        for (int i = 0; i < depth; ++i) {
            assert env != null;
            env = env.enclosing;
        }

        return env;
    }

    void assign(Token name, Object value)
    {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assignAt(int depth, Token name, Object value)
    {
        ancestor(depth).values.put(name.lexeme, value);
    }

    Object get(Token name)
    {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object getAt(int depth, Token name)
    {
        return getAt(depth, name.lexeme);
    }

    Object getAt(int depth, String name)
    {
        return ancestor(depth).values.get(name);
    }

    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;
}
