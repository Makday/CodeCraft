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
 *
 * Can be called with no arguments (breaks crosshair target) or with three arguments
 * representing relative block position from the player's perspective:
 *   - (0, 0, 1) = 1 block forward (where player is looking)
 *   - (1, 0, 0) = 1 block to the right
 *   - (-1, 0, 0) = 1 block to the left
 *   - (0, 1, 0) = 1 block up
 *   - (0, 0, -1) = 1 block backward
 *
 * Max distance is 4 blocks. Cannot break on the player's own position.
 */
public class BreakAction implements Action {
    private final Integer relX;
    private final Integer relY;
    private final Integer relZ;
    private static final int MAX_DISTANCE = 4;

    private boolean initialized = false;
    private BlockPos targetPos;
    private Direction targetSide;
    private BlockState initialState;

    /**
     * Break the block at the crosshair target.
     */
    public BreakAction() {
        this.relX = null;
        this.relY = null;
        this.relZ = null;
    }

    /**
     * Break a block at a relative position from the player's legs.
     * 
     * @param relX relative X coordinate (normalized: -4 to 4)
     * @param relY relative Y coordinate (normalized: -4 to 4)
     * @param relZ relative Z coordinate (normalized: -4 to 4)
     * @throws IllegalArgumentException if position is too far or on the player
     */
    public BreakAction(int relX, int relY, int relZ) {
        // Validate distance
        int maxDist = Math.max(Math.max(Math.abs(relX), Math.abs(relY)), Math.abs(relZ));
        if (maxDist > MAX_DISTANCE) {
            throw new IllegalArgumentException("Position too far: max distance is " + MAX_DISTANCE + " blocks");
        }
        // Validate not on player
        if (relX == 0 && relY == 0 && relZ == 0) {
            throw new IllegalArgumentException("Cannot break on the player's own position");
        }
        this.relX = relX;
        this.relY = relY;
        this.relZ = relZ;
    }

    private BlockPos getTargetBlockPos(MinecraftContext ctx) {
        ClientPlayerEntity player = ctx.player();
        net.minecraft.util.math.BlockPos origin = player.getBlockPos();
        float snappedYaw = TurnAction.computeNearestCardinalYaw(player.getYaw());

        int dx;
        int dz;
        switch ((int) snappedYaw) {
            case 0 -> {          // South
                dx = -relX;
                dz = relZ;
            }
            case 90 -> {         // West
                dx = -relZ;
                dz = -relX;
            }
            case 180 -> {        // North
                dx = relX;
                dz = -relZ;
            }
            case -90 -> {        // East
                dx = relZ;
                dz = relX;
            }
            default -> {
                dx = -relX;
                dz = relZ;
            }
        }

        return origin.add(dx, relY, dz);
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (ctx == null) return ActionStatus.DONE;
        MinecraftClient client = ctx.client();
        ClientPlayerEntity player = ctx.player();
        World world = ctx.world();
        if (client == null || player == null || world == null) return ActionStatus.DONE;

        try {
            // If relative coordinates are provided, break that specific block
            if (relX != null && relY != null && relZ != null) {
                BlockPos targetPos = getTargetBlockPos(ctx);
                BlockState blockState = world.getBlockState(targetPos);
                
                if (blockState.isAir()) {
                    return ActionStatus.DONE;
                }

                // Determine which face to break from (default to top)
                Direction side = Direction.UP;
                
                // Attempt to dig / attack the block this tick
                client.interactionManager.attackBlock(targetPos, side);
                player.swingHand(Hand.MAIN_HAND);
                
                // Check if block was broken (changed or is now air)
                BlockState currentState = world.getBlockState(targetPos);
                if (currentState.isAir() || !currentState.getBlock().equals(blockState.getBlock())) {
                    return ActionStatus.DONE;
                }
                
                return ActionStatus.RUNNING;
            }

            // Default behavior: use crosshair target
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

