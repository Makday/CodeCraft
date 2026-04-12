package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Moves an item from one inventory slot to another.
 * The inventory is organized as a grid with rows and columns.
 * Row 0-3: Main inventory (4 rows × 9 columns)
 * The operation requires the inventory screen to be open.
 */
public class MoveItemAction implements GameAction {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private boolean started = false;
    private int ticksRunning = 0;

    public MoveItemAction(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }

        ticksRunning++;

        // Validate slot coordinates
        if (!isValidSlot(fromRow, fromCol)) {
            return GameActionResult.failure("Invalid 'from' slot: row " + fromRow + ", col " + fromCol);
        }
        if (!isValidSlot(toRow, toCol)) {
            return GameActionResult.failure("Invalid 'to' slot: row " + toRow + ", col " + toCol);
        }

        // Initialize on first tick
        if (!started) {
            started = true;
            return performMove(context);
        }

        // After first tick, consider the operation complete
        // (Minecraft handles the inventory update)
        return GameActionResult.success();
    }

    /**
     * Performs the inventory move operation by directly manipulating the player's inventory.
     * This moves an item from one slot to another by swapping their contents.
     */
    private GameActionResult<Void> performMove(MinecraftContext context) {
        try {
            Inventory inventory = context.player().getInventory();

            // Convert grid coordinates to slot indices
            int fromSlot = rowColToSlot(fromRow, fromCol);
            int toSlot = rowColToSlot(toRow, toCol);

            // Check if the source slot has an item
            ItemStack fromStack = inventory.getStack(fromSlot);
            if (fromStack.isEmpty()) {
                return GameActionResult.failure("Source slot is empty: row " + fromRow + ", col " + fromCol);
            }

            // Get the destination stack
            ItemStack toStack = inventory.getStack(toSlot);

            // Perform the item move: swap the stacks
            inventory.setStack(toSlot, fromStack.copy());
            inventory.setStack(fromSlot, toStack.copy());

            return GameActionResult.success();

        } catch (Exception e) {
            return GameActionResult.failure("Failed to move item: " + e.getMessage());
        }
    }

    /**
     * Converts row and column coordinates to a linear slot index.
     * The inventory is 9 columns wide.
     */
    private int rowColToSlot(int row, int col) {
        return row * 9 + col;
    }

    /**
     * Validates that the slot coordinates are within valid bounds.
     * Main inventory: rows 0-3, cols 0-8 (indices 0-35)
     */
    private boolean isValidSlot(int row, int col) {
        if (row < 0 || row > 3 || col < 0 || col > 8) {
            return false;
        }
        return true;
    }
}
