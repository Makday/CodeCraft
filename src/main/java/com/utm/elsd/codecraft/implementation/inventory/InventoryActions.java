package com.utm.elsd.codecraft.implementation.inventory;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.ToolBarAction;

/**
 * Inventory-related actions for the DSL.
 */
public final class InventoryActions {

    private InventoryActions() {
        // Utility class
    }

    /**
     * Selects the given hotbar slot (0-8).
     */
    public static GameAction toolBar(int slot) {
        return new ToolBarAction(slot);
    }
}

