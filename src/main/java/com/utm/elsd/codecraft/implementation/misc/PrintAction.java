package com.utm.elsd.codecraft.implementation.misc;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.text.Text;

/**
 * Atomic action that prints a message to chat/system log.
 * Completes in one tick.
 */
public class PrintAction implements Action {
    private final String message;

    public PrintAction(String message) {
        this.message = message;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (ctx.client() != null && ctx.client().player != null) {
            ctx.client().player.sendMessage(Text.of(message), false);
        }
        return ActionStatus.DONE;
    }
}

