package com.utm.elsd.codecraft.implementation.movement.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.movement.input.InputOverride;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.util.shape.VoxelShape;

/**
 * An action to move the player forward by the specified number of blocks.
 */
public class MoveForwardAction implements Action {
    private static final double ARRIVAL_DISTANCE_SQ = 0.09; // 0.3 blocks
    private static final double SAMPLE_STEP_BLOCKS = 0.25;
    private static final int STUCK_TICKS_LIMIT = 12;
    private static final double MIN_PROGRESS_SQ = 0.0004; // 0.02 blocks
    private static final double MAX_STEP_UP = 0.6;
    private static final double MAX_STEP_DOWN = 0.6;
    private static final double PLAYER_HEIGHT = 1.8;
    private static final int SURFACE_SCAN_RADIUS = 2;

    private final int blocks;

    private boolean initialized;
    private boolean forcedForward;
    private double targetX = Double.NaN;
    private double targetZ = Double.NaN;
    private double lastDistanceSq = Double.NaN;
    private int stuckTicks;

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
        if (ctx == null || ctx.player() == null || ctx.world() == null) {
            stopForward();
            return ActionStatus.DONE;
        }

        if (!initialized) {
            initialized = true;
            double startFeetY = ctx.player().getY();
            initializeTarget(ctx);

            if (!isPathClear(ctx.world(), ctx.player().getX(), startFeetY, ctx.player().getZ(), targetX, targetZ, startFeetY)) {
                // Path is blocked at initialization, so do not execute movement.
                stopForward();
                return ActionStatus.DONE;
            }

            InputOverride.INSTANCE.force(InputOverride.Key.FORWARD);
            forcedForward = true;
        }

        double dx = ctx.player().getX() - targetX;
        double dz = ctx.player().getZ() - targetZ;
        double distanceSq = dx * dx + dz * dz;

        if (distanceSq <= ARRIVAL_DISTANCE_SQ) {
            stopForward();
            return ActionStatus.DONE;
        }

        if (!Double.isNaN(lastDistanceSq)) {
            if (lastDistanceSq - distanceSq < MIN_PROGRESS_SQ) {
                stuckTicks++;
                if (stuckTicks >= STUCK_TICKS_LIMIT) {
                    // Runtime fail-safe for dynamic obstacles or edge collisions.
                    stopForward();
                    return ActionStatus.DONE;
                }
            } else {
                stuckTicks = 0;
            }
        }

        lastDistanceSq = distanceSq;

        return ActionStatus.RUNNING;
    }

    static double[] computeTargetCenter(double startX, double startZ, float yaw, int blocks) {
        double rad = Math.toRadians(yaw);
        double projectedX = startX - Math.sin(rad) * blocks;
        double projectedZ = startZ + Math.cos(rad) * blocks;
        int blockX = (int) Math.floor(projectedX);
        int blockZ = (int) Math.floor(projectedZ);
        return new double[]{blockX + 0.5, blockZ + 0.5};
    }

    private void initializeTarget(MinecraftContext ctx) {
        double[] target = computeTargetCenter(ctx.player().getX(), ctx.player().getZ(), ctx.player().getYaw(), blocks);
        targetX = target[0];
        targetZ = target[1];
    }

    private static boolean isPathClear(World world, double startX, double startFeetY, double startZ, double endX, double endZ, double initialFeetY) {
        double dx = endX - startX;
        double dz = endZ - startZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= 1.0e-6) {
            return true;
        }

        int samples = Math.max(1, (int) Math.ceil(distance / SAMPLE_STEP_BLOCKS));
        double previousFeetY = initialFeetY;
        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;
            double sampleX = startX + dx * t;
            double sampleZ = startZ + dz * t;

            Double candidateFeetY = findWalkableFeetY(world, sampleX, sampleZ, previousFeetY);
            if (candidateFeetY == null) {
                return false;
            }

            previousFeetY = candidateFeetY;
        }

        return true;
    }

    private static Double findWalkableFeetY(World world, double x, double z, double previousFeetY) {
        BlockPos origin = BlockPos.ofFloored(x, previousFeetY, z);

        Double best = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (int dy = -SURFACE_SCAN_RADIUS; dy <= SURFACE_SCAN_RADIUS; dy++) {
            BlockPos supportPos = origin.add(0, dy, 0);
            double surfaceY = surfaceY(world, supportPos);
            if (Double.isNaN(surfaceY)) {
                continue;
            }

            if (!isStepAllowed(previousFeetY, surfaceY)) {
                continue;
            }

            if (!hasHeadroom(world, x, z, surfaceY)) {
                continue;
            }

            double distance = Math.abs(surfaceY - previousFeetY);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = surfaceY;
            }
        }

        return best;
    }

    private static boolean isStepAllowed(double fromFeetY, double toFeetY) {
        double delta = toFeetY - fromFeetY;
        return delta <= MAX_STEP_UP && delta >= -MAX_STEP_DOWN;
    }

    static boolean isStepAllowedForTest(double fromFeetY, double toFeetY) {
        return isStepAllowed(fromFeetY, toFeetY);
    }

    private static double surfaceY(World world, BlockPos supportPos) {
        BlockState state = world.getBlockState(supportPos);
        VoxelShape shape = state.getCollisionShape(world, supportPos);
        if (shape.isEmpty()) {
            return Double.NaN;
        }

        return supportPos.getY() + shape.getMax(Direction.Axis.Y);
    }

    private static boolean hasHeadroom(World world, double x, double z, double feetY) {
        int minY = BlockPos.ofFloored(x, feetY + 0.01, z).getY() + 1;
        int maxY = BlockPos.ofFloored(x, feetY + PLAYER_HEIGHT - 0.01, z).getY();

        for (int y = minY; y <= maxY; y++) {
            BlockPos pos = BlockPos.ofFloored(x, y, z);
            BlockState state = world.getBlockState(pos);
            if (!state.getCollisionShape(world, pos).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void stopForward() {
        if (forcedForward) {
            InputOverride.INSTANCE.release(InputOverride.Key.FORWARD);
            forcedForward = false;
        }
    }
}
