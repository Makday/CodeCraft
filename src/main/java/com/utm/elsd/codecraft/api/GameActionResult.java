package com.utm.elsd.codecraft.api;

/**
 * Represents the result of executing a GameAction.
 * Provides success/failure status, error messages, and optional return values.
 */
public class GameActionResult<T> {

    private final boolean success;
    private final String errorMessage;
    private final T value;

    // Private constructors - use static factory methods
    private GameActionResult(boolean success, String errorMessage, T value) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.value = value;
    }

    /**
     * Creates a successful result with no return value.
     */
    public static <T> GameActionResult<T> success() {
        return new GameActionResult<>(true, null, null);
    }

    /**
     * Creates a successful result with a return value.
     */
    public static <T> GameActionResult<T> success(T value) {
        return new GameActionResult<>(true, null, value);
    }

    /**
     * Creates a failed result with an error message.
     */
    public static <T> GameActionResult<T> failure(String errorMessage) {
        return new GameActionResult<>(false, errorMessage, null);
    }

    /**
     * Returns true if the action executed successfully.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the error message if the action failed, null otherwise.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the return value if the action succeeded and provided one, null otherwise.
     */
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (success) {
            return value != null ? "SUCCESS: " + value : "SUCCESS";
        } else {
            return "FAILED: " + errorMessage;
        }
    }
}
