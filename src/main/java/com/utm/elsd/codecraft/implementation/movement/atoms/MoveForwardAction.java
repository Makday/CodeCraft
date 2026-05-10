package com.utm.elsd.codecraft.implementation.movement.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.movement.input.InputOverride;

/**
 * An action to move the player forward by the specified number of blocks.
 */
public class MoveForwardAction implements Action {
    private final int blocks;
    private double targetX = Double.NaN;
    private double targetZ = Double.NaN;

    /**
     * An action to move the player forward by the specified number of blocks.
     * Movement happens gradually over multiple game ticks.
     *
     * @param blocks The number of blocks to move forward
     */
    public MoveForwardAction(int blocks) {
        if (blocks <= 0) throw new IllegalArgumentException("blocks must be positive, got: " + blocks);
        this.blocks = blocks;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (Double.isNaN(targetX)) {
            float yaw = ctx.player().getYaw();
            double rad = Math.toRadians(yaw);
            targetX = ctx.player().getX() - Math.sin(rad) * blocks;
            targetZ = ctx.player().getZ() + Math.cos(rad) * blocks;
            InputOverride.INSTANCE.force(InputOverride.Key.FORWARD);
        }

        double dx = ctx.player().getX() - targetX;
        double dz = ctx.player().getZ() - targetZ;

        if (dx * dx + dz * dz <= 0.25) {
            InputOverride.INSTANCE.release(InputOverride.Key.FORWARD);
            return ActionStatus.DONE;
        }

        return ActionStatus.RUNNING;
    }
}
