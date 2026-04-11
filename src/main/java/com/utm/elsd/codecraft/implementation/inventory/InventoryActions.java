package com.utm.elsd.codecraft.implementation.inventory;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.DropItemAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.ToolBarAction;
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
      
    /**
     * Selects the given hotbar slot (0-8).
     */
    public static GameAction toolBar(int slot) {
        return new ToolBarAction(slot);
    }

}
