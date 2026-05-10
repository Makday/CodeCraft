package com.utm.elsd.codecraft.dsl.interpreter;

import com.utm.elsd.codecraft.api.Action;

import java.util.List;
import java.util.Map;

/**
 * Result of running a DSL program.
 */
public record InterpreterResult(
        Value lastValue,
        List<Action> actions,
        Map<String, Value> variables,
        List<String> logs,
        boolean stopped
) {
    public InterpreterResult {
        actions = List.copyOf(actions);
        variables = Map.copyOf(variables);
        logs = List.copyOf(logs);
    }
}

