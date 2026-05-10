package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.helper.InventoryHelper;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class MoveItemAction implements Action {

    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;

    /**
     * An action that moves an item from one inventory slot to another.
     *
     * @param fromRow The row of the source slot (0-3)
     * @param fromCol The column of the source slot (0-8)
     * @param toRow The row of the destination slot (0-3)
     * @param toCol The column of the destination slot (0-8)
     * @return A GameAction that will execute the item move
     */
    public MoveItemAction(int fromRow, int fromCol, int toRow, int toCol) {
        if (!InventoryHelper.isValidSlot(fromRow, fromCol)) {
            throw new IllegalArgumentException("Invalid 'from' slot: row=" + fromRow + ", col=" + fromCol);
        }
        if (!InventoryHelper.isValidSlot(toRow, toCol)) {
            throw new IllegalArgumentException("Invalid 'to' slot: row=" + toRow + ", col=" + toCol);
        }
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        PlayerScreenHandler handler = ctx.player().playerScreenHandler;

        int fromSlot = InventoryHelper.calculateScreenSlot(fromRow, fromCol);
        int toSlot = InventoryHelper.calculateScreenSlot(toRow, toCol);

        // if slot is empty, the action is done
        if (handler.getSlot(fromSlot).getStack().isEmpty()) return ActionStatus.DONE;

        boolean toSlotHasItem = !handler.getSlot(toSlot).getStack().isEmpty();

        ctx.client().interactionManager.clickSlot(
                handler.syncId, fromSlot, 0, SlotActionType.PICKUP, ctx.player()
        );

        ctx.client().interactionManager.clickSlot(
                handler.syncId, toSlot, 0, SlotActionType.PICKUP, ctx.player()
        );

        if (toSlotHasItem) {
            ctx.client().interactionManager.clickSlot(
                    handler.syncId, fromSlot, 0, SlotActionType.PICKUP, ctx.player()
            );
        }

        return ActionStatus.DONE;
    }
}