package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Action that selects a hotbar slot (0-8) for the player.
 */
public class ToolBarAction implements GameAction {
    private final int slot;

    public ToolBarAction(int slot) {
        this.slot = slot;
    }

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }

        if (slot < 0 || slot > 8) {
            return GameActionResult.failure("Invalid hotbar slot: " + slot + " (must be 0-8)");
        }

        context.setSelectedHotbarSlot(slot);
        return GameActionResult.success();
    }
}

