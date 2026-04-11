package com.utm.elsd.codecraft.api;

import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.InventoryActions;
import com.utm.elsd.codecraft.implementation.movement.MovementActions;

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
    public GameAction createAction(String functionName, Object... params) {
        switch (functionName) {
            case "move_forward":
                if (params.length == 1 && params[0] instanceof Integer) {
                    return MovementActions.moveForward((Integer) params[0]);
                }
                break;
            
            case "drop_item":
                if (params.length == 2 && params[0] instanceof Integer && params[1] instanceof Integer) {
                    return InventoryActions.dropItem((Integer) params[0], (Integer) params[1]);
                }
                break;
            
            case "tool_bar":
                if (params.length == 1 && params[0] instanceof Integer) {
                    return InventoryActions.toolBar((Integer) params[0]);
                }
                break;

            case "open_inventory":
                return InventoryActions.openInventory();

            case "close_inventory":
                return InventoryActions.closeInventory();

            case "move_item":
                if (params.length == 4 && params[0] instanceof Integer && params[1] instanceof Integer 
                    && params[2] instanceof Integer && params[3] instanceof Integer) {
                    return InventoryActions.moveItem((Integer) params[0], (Integer) params[1], 
                                                     (Integer) params[2], (Integer) params[3]);
                }
                break;

            // add more cases here as we implement other actions
        }
        return null; // unknown function
    }
}
