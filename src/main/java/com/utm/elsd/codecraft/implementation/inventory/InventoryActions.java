package com.utm.elsd.codecraft.implementation.inventory;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.DropItemAction;

/**
 * Inventory-related actions for the DSL.
 */
public final class InventoryActions {

    private InventoryActions() {
        // Utility class
    }

    /**
     * Creates an action that drops the entire item stack from inventory slot (row, col).
     *
     * @param row 0–2 for main inventory rows (top to bottom), 3 for hotbar
     * @param col 0–8 (left to right)
     * @return A GameAction that will execute the drop
     */
    public static GameAction dropItem(int row, int col) {
        return new DropItemAction(row, col);
    }
}
