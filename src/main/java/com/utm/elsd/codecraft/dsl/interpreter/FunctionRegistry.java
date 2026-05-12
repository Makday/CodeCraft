package com.utm.elsd.codecraft.dsl.interpreter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for DSL functions.
 *
 * New functions can be registered without changing the interpreter itself.
 */
public final class FunctionRegistry {
    private final Map<String, InterpreterFunction> functions = new LinkedHashMap<>();

    public FunctionRegistry register(String name, InterpreterFunction function) {
        String key = normalize(name);
        functions.put(key, Objects.requireNonNull(function, "function"));
        return this;
    }

    public boolean has(String name) {
        return functions.containsKey(normalize(name));
    }

    public Value invoke(String name, ExecutionContext context) {
        InterpreterFunction function = functions.get(normalize(name));
        if (function == null) {
            throw new InterpreterException("Unknown function: " + name);
        }
        Value result = function.invoke(context);
        return result == null ? Value.nullValue() : result;
    }

    public FunctionRegistry copy() {
        FunctionRegistry registry = new FunctionRegistry();
        registry.functions.putAll(functions);
        return registry;
    }

    private String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new InterpreterException("Function name must not be blank");
        }
        return name;
    }
}

