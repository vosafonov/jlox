package com.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    void define(String name, Object value)
    {
        values.put(name, value);
    }

    Object get(Token name)
    {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    private final Map<String, Object> values = new HashMap<>();
}
