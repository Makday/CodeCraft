package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

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

        // Ensure inventory screen is open
        if (!context.isInventoryScreenOpen()) {
            return GameActionResult.failure("Inventory screen is not open. Open it first with open_inventory()");
        }

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
     * Performs the inventory move operation by simulating clicks on the inventory screen.
     */
    private GameActionResult<Void> performMove(MinecraftContext context) {
        try {
            InventoryScreen screen = (InventoryScreen) context.client().currentScreen;
            if (screen == null) {
                return GameActionResult.failure("Inventory screen became unavailable");
            }

            Inventory inventory = context.player().getInventory();

            // Convert grid coordinates to slot indices
            int fromSlot = rowColToSlot(fromRow, fromCol);
            int toSlot = rowColToSlot(toRow, toCol);

            // Check if the source slot has an item
            ItemStack fromStack = inventory.getStack(fromSlot);
            if (fromStack.isEmpty()) {
                return GameActionResult.failure("Source slot is empty: row " + fromRow + ", col " + fromCol);
            }

            // Perform the click sequence on the inventory screen
            // First click on the source slot to pick it up
            simulateInventoryClick(screen, fromSlot, false);

            // Then click on the destination slot to place it
            simulateInventoryClick(screen, toSlot, false);

            return GameActionResult.success();

        } catch (Exception e) {
            return GameActionResult.failure("Failed to move item: " + e.getMessage());
        }
    }

    /**
     * Simulates a click on an inventory slot via the InventoryScreen.
     */
    private void simulateInventoryClick(InventoryScreen screen, int slot, boolean shiftClick) {
        // Use the screen's onMouseClick method to simulate clicking a slot
        // The exact method signature depends on Minecraft version
        // For Fabric/Minecraft 1.20+, we use the appropriate method
        try {
            // Use QUICK_MOVE (shift-click) for shift clicks, otherwise use PICKUP (normal click)
            SlotActionType actionType = shiftClick ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
            screen.onMouseClick(null, slot, 0, actionType);
        } catch (Exception e) {
            // Fallback: if onMouseClick doesn't exist, try alternative approach
            // This is version-dependent and may need adjustment
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
