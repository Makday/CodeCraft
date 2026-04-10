package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;

public class CloseInventoryAction implements GameAction {

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {

        if (!context.isScreenOpen()) {
            return GameActionResult.failure("Already closed.");
        }

        context.setScreen(null);
        return GameActionResult.success();
    }
}