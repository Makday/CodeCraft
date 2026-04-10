package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;

public abstract class InventoryAction implements GameAction {

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }
        if (context.client() == null) {
            return GameActionResult.failure("Minecraft client not available");
        }
        if (context.client().player == null) {
            return GameActionResult.failure("Player not available");
        }
        return executeInventoryAction(context);
    }

    protected abstract GameActionResult<Void> executeInventoryAction(MinecraftContext context);
}
