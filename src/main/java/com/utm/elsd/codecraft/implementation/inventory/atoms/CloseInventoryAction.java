package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;

public class CloseInventoryAction extends InventoryAction {

    @Override
    protected GameActionResult<Void> executeInventoryAction(MinecraftContext context) {
        if (!context.isInventoryOpen()) {
            return GameActionResult.failure("Already closed.");
        }

        context.setScreen(null);
        return GameActionResult.success();
    }
}