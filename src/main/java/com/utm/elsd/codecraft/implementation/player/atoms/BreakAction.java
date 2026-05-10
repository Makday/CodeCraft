package com.utm.elsd.codecraft.implementation.player.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;

/**
 * Holds left click (attack) on the block the player is currently looking at until it is broken.
 */
public class BreakAction implements Action {

    private boolean initialized = false;
    private BlockPos targetPos;
    private Direction targetSide;
    private BlockState initialState;

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (ctx == null) return ActionStatus.DONE;
        MinecraftClient client = ctx.client();
        ClientPlayerEntity player = ctx.player();
        World world = ctx.world();
        if (client == null || player == null || world == null) return ActionStatus.DONE;

        try {
            if (!initialized) {
                HitResult hit = client.crosshairTarget;
                if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
                    return ActionStatus.DONE;
                }

                BlockHitResult blockHit = (BlockHitResult) hit;
                targetPos = blockHit.getBlockPos();
                targetSide = blockHit.getSide();
                initialState = world.getBlockState(targetPos);
                // If there is nothing to break, finish immediately
                if (initialState.isAir()) return ActionStatus.DONE;
                initialized = true;
            }

            // If the block at the target pos is already gone or changed, we're done
            BlockState current = world.getBlockState(targetPos);
            if (current.isAir() || !current.getBlock().equals(initialState.getBlock())) {
                player.swingHand(Hand.MAIN_HAND);
                return ActionStatus.DONE;
            }

            // Attempt to dig / attack the block this tick
            client.interactionManager.attackBlock(targetPos, targetSide);
            player.swingHand(Hand.MAIN_HAND);
        } catch (Throwable ignored) {
            // resilient: don't crash the interpreter on unexpected client internals
            return ActionStatus.DONE;
        }

        return ActionStatus.RUNNING;
    }
}

