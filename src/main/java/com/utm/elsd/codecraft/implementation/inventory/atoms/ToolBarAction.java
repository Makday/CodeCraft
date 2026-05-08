package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Action that selects a hotbar slot (0-8) for the player.
 */
public class ToolBarAction implements Action {
    private final int slot;

    public ToolBarAction(int slot) {
        this.slot = slot;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (slot < 0 || slot > 8) {
            throw new IllegalArgumentException("Invalid slot number");
        }
        ctx.setSelectedHotbarSlot(slot);
        return ActionStatus.DONE;
    }
}

