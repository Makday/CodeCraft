package com.utm.elsd.codecraft.dsl.interpreter;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.context.MinecraftContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Context passed to interpreter functions.
 *
 * Functions can inspect variables, emit actions, and log messages without being
 * coupled to the AST traversal logic.
 */
public final class ExecutionContext {
    private final Interpreter interpreter;
    private final Environment environment;
    private final MinecraftContext minecraftContext;
    private final List<Action> actions;
    private final List<String> logs;
    private final List<Value> arguments;
    private final Consumer<String> logger;

    ExecutionContext(
            Interpreter interpreter,
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs,
            List<Value> arguments,
            Consumer<String> logger
    ) {
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.minecraftContext = minecraftContext;
        this.actions = Objects.requireNonNull(actions, "actions");
        this.logs = Objects.requireNonNull(logs, "logs");
        this.arguments = List.copyOf(arguments);
        this.logger = logger;
    }

    public Interpreter interpreter() {
        return interpreter;
    }

    public Environment environment() {
        return environment;
    }

    public MinecraftContext minecraftContext() {
        return minecraftContext;
    }

    public List<Value> arguments() {
        return arguments;
    }

    public Value argument(int index) {
        if (index < 0 || index >= arguments.size()) {
            throw new InterpreterException(
                    "Expected at least " + (index + 1) + " argument(s) but got " + arguments.size());
        }
        return arguments.get(index);
    }

    public void requireArity(String functionName, int expected) {
        if (arguments.size() != expected) {
            throw new InterpreterException(
                    functionName + " expects " + expected + " argument(s) but got " + arguments.size());
        }
    }

    public void emit(Action action) {
        actions.add(Objects.requireNonNull(action, "action"));
    }

    public void log(String message) {
        String resolved = message == null ? "null" : message;
        logs.add(resolved);
        if (logger != null) {
            logger.accept(resolved);
        }
    }
}

