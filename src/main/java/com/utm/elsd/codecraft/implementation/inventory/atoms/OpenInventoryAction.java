package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class OpenInventoryAction implements GameAction {

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (context.isInventoryScreenOpen()) {
            return GameActionResult.failure("Inventory is already open.");
        }
        context.setScreen(new InventoryScreen(context.player()));
        return GameActionResult.success();
    }
}