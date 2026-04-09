package com.utm.elsd.codecraft.implementation.inventory;

/**
 * Inventory-related actions for the DSL.
 *
 * Placeholder for future implementations such as:
 * open_inventory, close_inventory, drop_item, move_item, tool_bar.
 */
public final class InventoryActions {

    private InventoryActions() {
        // Utility class
    }

    /**
     * Selects the given hotbar slot (0-8).
     */
    public static com.utm.elsd.codecraft.api.GameAction toolBar(int slot) {
        return new com.utm.elsd.codecraft.implementation.inventory.atoms.ToolBarAction(slot);
    }
}

