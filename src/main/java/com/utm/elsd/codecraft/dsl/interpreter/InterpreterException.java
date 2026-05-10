package com.utm.elsd.codecraft.dsl.interpreter;

/**
 * Thrown when the interpreter cannot evaluate a program.
 */
public class InterpreterException extends RuntimeException {
    public InterpreterException(String message) {
        super(message);
    }

    public InterpreterException(String message, Throwable cause) {
        super(message, cause);
    }
}

