package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Drops the entire item stack from the inventory slot at (row, col).
 *
 * Inventory layout:
 *   row 0 — top row of main inventory    (screen slots  9–17)
 *   row 1 — middle row of main inventory (screen slots 18–26)
 *   row 2 — bottom row of main inventory (screen slots 27–35)
 *   row 3 — hotbar                       (screen slots 36–44)
 *
 * col is 0–8 in all rows.
 */

public class DropItemAction implements GameAction {

    private final int row;
    private final int col;

    public DropItemAction(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public GameActionResult<?> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }

        if (row < 0 || row > 3 || col < 0 || col > 8) {
            return GameActionResult.failure(
                    "Invalid slot: row=" + row + ", col=" + col);
        }

        int screenSlot = calculateScreenSlot(row, col);
        ItemStack stack = context.player().playerScreenHandler.getSlot(screenSlot).getStack();
        if (stack.isEmpty()) {
            return GameActionResult.failure("No item at slot (row=" + row + ", col=" + col + ")");
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int syncId = context.player().playerScreenHandler.syncId;

        // SlotActionType.THROW + button 1 = drop the entire stack
        client.interactionManager.clickSlot(syncId, screenSlot, 1, SlotActionType.THROW, context.player());

        return GameActionResult.success();
    }

    /**
     * Maps (row, col) to the slot index used by PlayerScreenHandler.
     *
     * PlayerScreenHandler layout:
     *   0        – crafting output
     *   1–4      – crafting grid
     *   5–8      – armor
     *   9–35     – main inventory (rows 0–2, left-to-right, top-to-bottom)
     *   36–44    – hotbar (row 3)
     *   45       – off-hand
     */

    private int calculateScreenSlot(int row, int col) {
        if (row == 3) {
            return 36 + col;
        }
        return 9 + row * 9 + col;
    }
}