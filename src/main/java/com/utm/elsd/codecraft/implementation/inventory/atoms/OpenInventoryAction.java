package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * Action that opens the player's inventory screen.
 */
public class OpenInventoryAction implements Action {

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (!ctx.isScreenOpen()) {
            ctx.setScreen(new InventoryScreen(ctx.player()));
        }
        return ActionStatus.DONE;
    }
}