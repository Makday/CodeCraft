package com.utm.elsd.codecraft.dsl.interpreter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Mutable runtime environment for variables.
 *
 * The current language uses straightforward assignment semantics, so the
 * interpreter keeps a single global scope but still models scopes as a stack so
 * future features such as functions can add nested scopes without changing the
 * public API.
 */
public final class Environment {
    private final Deque<Map<String, Value>> scopes = new ArrayDeque<>();

    public Environment() {
        scopes.push(new LinkedHashMap<>());
    }

    public void pushScope() {
        scopes.push(new LinkedHashMap<>());
    }

    public void popScope() {
        if (scopes.size() == 1) {
            throw new InterpreterException("Cannot pop the root scope");
        }
        scopes.pop();
    }

    public void assign(String name, Value value) {
        for (Map<String, Value> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        scopes.peek().put(name, value);
    }

    public Value get(String name) {
        for (Map<String, Value> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new InterpreterException("Undefined variable: " + name);
    }

    public boolean contains(String name) {
        for (Map<String, Value> scope : scopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Value> snapshot() {
        Map<String, Value> snapshot = new LinkedHashMap<>();
        for (Iterator<Map<String, Value>> iterator = scopes.descendingIterator(); iterator.hasNext(); ) {
            snapshot.putAll(iterator.next());
        }
        return Map.copyOf(snapshot);
    }
}


