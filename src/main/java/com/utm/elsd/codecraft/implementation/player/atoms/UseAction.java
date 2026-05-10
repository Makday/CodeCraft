package com.utm.elsd.codecraft.implementation.player.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Performs a right-click (use) with the currently held item.
 *
 * This implementation mirrors vanilla right-click flow by first checking the
 * current crosshair target (block/entity) and only falling back to item use.
 */
public class UseAction implements Action {

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (ctx == null) return ActionStatus.DONE;

        MinecraftClient client = ctx.client();
        ClientPlayerEntity player = ctx.player();
        if (client == null || player == null) return ActionStatus.DONE;

        if (client.interactionManager == null) {
            return ActionStatus.DONE;
        }

        try {
            boolean handled = false;
            HitResult hitResult = client.crosshairTarget;

            if (hitResult != null) {
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    ActionResult result = client.interactionManager.interactBlock(
                            player,
                            Hand.MAIN_HAND,
                            (BlockHitResult) hitResult
                    );
                    handled = result != ActionResult.PASS;
                } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hitResult;
                    Entity entity = entityHit.getEntity();

                    ActionResult result = client.interactionManager.interactEntityAtLocation(
                            player,
                            entity,
                            entityHit,
                            Hand.MAIN_HAND
                    );
                    if (result == ActionResult.PASS) {
                        result = client.interactionManager.interactEntity(player, entity, Hand.MAIN_HAND);
                    }
                    handled = result != ActionResult.PASS;
                }
            }

            if (!handled) {
                ActionResult itemResult = client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                handled = itemResult != ActionResult.PASS;
            }

            if (handled) {
                player.swingHand(Hand.MAIN_HAND);
            }
        } catch (Throwable ignored) {
            // Keep the interpreter resilient: if interaction fails, do not crash.
        }

        return ActionStatus.DONE;
    }
}




