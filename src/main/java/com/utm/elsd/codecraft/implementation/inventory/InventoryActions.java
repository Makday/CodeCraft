package com.utm.elsd.codecraft.implementation.inventory;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.CloseInventoryAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.OpenInventoryAction;

/**
 * Inventory-related actions for the DSL.
 */

public class InventoryActions {

    /**
     * Creates an action that opens the player's inventory screen.
     *
     * @return A GameAction that will open the inventory
     */
    public static GameAction openInventory() {
        return new OpenInventoryAction();
    }

    /**
     * Creates an action that closes the player's inventory screen.
     *
     * @return A GameAction that will close the inventory
     */
    public static GameAction closeInventory() {
        return new CloseInventoryAction();
    }
}
