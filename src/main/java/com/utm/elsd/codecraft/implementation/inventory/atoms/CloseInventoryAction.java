package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Action that closes the player's inventory screen.
 */
public class CloseInventoryAction implements Action {

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        ctx.setScreen(null);
        return ActionStatus.DONE;
    }
}