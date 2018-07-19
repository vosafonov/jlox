package com.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    LoxInstance(LoxClass klass)
    {
        this.klass = klass;
    }

    @Override
    public String toString()
    {
        return klass.name + " instance";
    }

    private LoxClass klass;
}
