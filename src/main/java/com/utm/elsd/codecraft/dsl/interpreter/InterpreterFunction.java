package com.utm.elsd.codecraft.dsl.interpreter;

@FunctionalInterface
public interface InterpreterFunction {
    Value invoke(ExecutionContext context);
}

