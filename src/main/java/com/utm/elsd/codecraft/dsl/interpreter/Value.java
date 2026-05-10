package com.utm.elsd.codecraft.dsl.interpreter;

import java.util.Objects;

/**
 * Runtime value used by the interpreter.
 *
 * The DSL currently works with primitive-like values only, so this wrapper keeps
 * the execution layer extensible without tying AST evaluation to Java types.
 */
public final class Value {
    private static final Value NULL = new Value(null);

    private final Object raw;

    private Value(Object raw) {
        this.raw = raw;
    }

    public static Value nullValue() {
        return NULL;
    }

    public static Value of(Object value) {
        if (value == null) {
            return NULL;
        }
        if (value instanceof Value existing) {
            return existing;
        }
        return new Value(value);
    }

    public Object raw() {
        return raw;
    }

    public boolean isNull() {
        return raw == null;
    }

    public boolean isNumber() {
        return raw instanceof Number;
    }

    public boolean isBoolean() {
        return raw instanceof Boolean;
    }

    public boolean isString() {
        return raw instanceof String;
    }

    public long asLong() {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        if (raw instanceof Boolean bool) {
            return bool ? 1L : 0L;
        }
        if (raw instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                throw new InterpreterException("Cannot convert string to number: " + string, e);
            }
        }
        throw new InterpreterException("Expected a number but got: " + describe());
    }

    public int asInt() {
        return Math.toIntExact(asLong());
    }

    public boolean asBoolean() {
        if (raw instanceof Boolean bool) {
            return bool;
        }
        if (raw instanceof Number number) {
            return number.doubleValue() != 0.0d;
        }
        if (raw instanceof String string) {
            return !string.isEmpty();
        }
        return raw != null;
    }

    public String asString() {
        if (raw == null) {
            return "null";
        }
        return String.valueOf(raw);
    }

    public boolean isTruthy() {
        return asBoolean();
    }

    public String describe() {
        return raw == null ? "null" : raw + " (" + raw.getClass().getSimpleName() + ")";
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Value value)) {
            return false;
        }
        return Objects.equals(raw, value.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(raw);
    }
}

