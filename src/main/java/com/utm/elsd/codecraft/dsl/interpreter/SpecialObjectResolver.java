package com.utm.elsd.codecraft.dsl.interpreter;

@FunctionalInterface
public interface SpecialObjectResolver {
    Value resolve(String object, String field, ExecutionContext context);
}

