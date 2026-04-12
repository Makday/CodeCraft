package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.helper.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.ScreenHandler;
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

        if (!isValidSlot(fromRow, fromCol)) {
            return GameActionResult.failure("Invalid 'from' slot: row " + fromRow + ", col " + fromCol);
        }
        if (!isValidSlot(toRow, toCol)) {
            return GameActionResult.failure("Invalid 'to' slot: row " + toRow + ", col " + toCol);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerScreenHandler handler = client.player.playerScreenHandler;

        int fromSlot = InventoryHelper.calculateScreenSlot(fromRow, fromCol);
        int toSlot = InventoryHelper.calculateScreenSlot(toRow, toCol);

        if (fromSlot == -1 || toSlot == -1) {
            return GameActionResult.failure("Slot coordinates could not be mapped to screen handler");
        }

        if (handler.getSlot(fromSlot).getStack().isEmpty()) {
            return GameActionResult.failure("Source slot is empty: row " + fromRow + ", col " + fromCol);
        }

        boolean toSlotHasItem = !handler.getSlot(toSlot).getStack().isEmpty();

        client.interactionManager.clickSlot(
                handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player
        );

        client.interactionManager.clickSlot(
                handler.syncId, toSlot, 0, SlotActionType.PICKUP, client.player
        );

        if (toSlotHasItem) {
            client.interactionManager.clickSlot(
                    handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player
            );
        }

        return GameActionResult.success();
    }

    private boolean isValidSlot(int row, int col) {
        return row >= 0 && row <= 3 && col >= 0 && col <= 8;
    }
}
