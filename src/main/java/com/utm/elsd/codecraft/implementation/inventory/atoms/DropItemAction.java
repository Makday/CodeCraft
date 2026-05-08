package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.helper.InventoryHelper;
import net.minecraft.screen.slot.SlotActionType;
/**
 * An action that drops the entire item stack from inventory slot (row, col).
 */
public class DropItemAction implements Action {

    private final int row;
    private final int col;

    /**
     * An action that drops the entire item stack from inventory slot (row, col).
     *
     * @param row 0–2 for main inventory rows (top to bottom), 3 for hotbar
     * @param col 0–8 (left to right)
     */
    public DropItemAction(int row, int col) {
        if (!InventoryHelper.isValidSlot(row, col)) {
            throw new IllegalArgumentException("Invalid slot: row=" + row + ", col=" + col);
        }
        this.row = row;
        this.col = col;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        int screenSlot = InventoryHelper.calculateScreenSlot(row, col);

        //  if slot is not empty, do the action, else it is done
        if (!ctx.player().playerScreenHandler.getSlot(screenSlot).getStack().isEmpty()) {
            ctx.client().interactionManager.clickSlot(
                    ctx.player().playerScreenHandler.syncId,
                    screenSlot,
                    1,
                    SlotActionType.THROW,
                    ctx.player()
            );
        }

        return ActionStatus.DONE;
    }
}

/**
 * Inventory layout:
 *   row 0 — top row of main inventory    (screen slots  9–17)
 *   row 1 — middle row of main inventory (screen slots 18–26)
 *   row 2 — bottom row of main inventory (screen slots 27–35)
 *   row 3 — hotbar                       (screen slots 36–44)
 *
 * col is 0–8 in all rows.
 */