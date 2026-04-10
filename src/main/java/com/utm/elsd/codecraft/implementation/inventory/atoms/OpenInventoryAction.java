package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class OpenInventoryAction extends InventoryAction {

    @Override
    protected GameActionResult<Void> executeInventoryAction(MinecraftContext context) {
        if (context.isInventoryOpen()) {
            return GameActionResult.success();
        }
        context.setScreen(new InventoryScreen(context.client().player));
        return GameActionResult.success();
    }
}