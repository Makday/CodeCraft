package com.utm.elsd.codecraft.api;

import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Executes GameActions and manages their lifecycle.
 * This is the main entry point for the DSL interpreter to execute actions.
 */
public class GameActionExecutor {

    private final MinecraftContext context;

    public GameActionExecutor(MinecraftContext context) {
        this.context = context;
    }

    /**
     * Executes an action by name with the given parameters.
     * This is the primary method used by the DSL interpreter.
     *
     * @param functionName The name of the function to execute (e.g., "moveForward", "dropItem")
     * @param params The parameters for the function
     * @return The result of the action execution
     */
    public GameActionResult<?> executeAction(String functionName, Object... params) {
        try {
            GameAction action = createAction(functionName, params);
            if (action == null) {
                return GameActionResult.failure("Unknown function: " + functionName);
            }
            return action.execute(context);
        } catch (Exception e) {
            return GameActionResult.failure("Execution failed: " + e.getMessage());
        }
    }

    /**
     * Executes a GameAction object directly.
     *
     * @param action The action to execute
     * @return The result of the action execution
     */
    public GameActionResult<?> executeAction(GameAction action) {
        try {
            return action.execute(context);
        } catch (Exception e) {
            return GameActionResult.failure("Execution failed: " + e.getMessage());
        }
    }

    /**
     * Creates a GameAction from a function name and parameters.
     * This method contains the mapping from DSL function names to action implementations.
     *
     * @param functionName The function name
     * @param params The parameters
     * @return The created GameAction, or null if function is unknown
     */
    private GameAction createAction(String functionName, Object[] params) {
        // This will be implemented when we create the Minecraft action classes
        // For now, return null to indicate unknown function
        return null;
    }
}
