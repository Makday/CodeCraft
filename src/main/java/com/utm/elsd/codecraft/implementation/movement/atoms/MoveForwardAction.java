package com.utm.elsd.codecraft.implementation.movement.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.movement.helpers.ForwardMovementInput;
import net.minecraft.util.math.Vec3d;

public class MoveForwardAction implements GameAction {
    private final int blocks;
    private Vec3d targetPosition;
    private boolean started = false;
    private int ticksRunning = 0;
    private double initialDistance = 0;

    public MoveForwardAction(int blocks) {
        this.blocks = blocks;
    }

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }

        ticksRunning++;

        // Calculate target position on first execution
        if (!started) {
            Vec3d currentPos = context.position();
            Vec3d facing = context.facingDirection();

            // Calculate target position: current + (facing * blocks)
            targetPosition = currentPos.add(facing.multiply(blocks));

            // Store initial distance for progress tracking
            initialDistance = calculateDistance(currentPos, targetPosition);

            // Set player input to move forward
            context.setInput(new ForwardMovementInput());
            started = true;

            return GameActionResult.failure("Movement started - target: " + formatPosition(targetPosition));
        }

        // Check current position
        Vec3d currentPos = context.position();
        double currentDistance = calculateDistance(currentPos, targetPosition);

        // PRIMARY COMPLETION: Very precise distance check (0.05 blocks)
        if (currentDistance < 0.05) {
            context.restoreDefaultInput();
            return GameActionResult.success();
        }

        // SECONDARY COMPLETION: Close enough after reasonable time (0.1 blocks after 2 seconds)
        if (ticksRunning > 40 && currentDistance < 0.1) {
            context.restoreDefaultInput();
            return GameActionResult.success();
        }

        // TERTIARY COMPLETION: Player stopped moving and very close (0.15 blocks)
        Vec3d velocity = context.velocity();
        double playerSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (playerSpeed < 0.01 && currentDistance < 0.15) {
            context.restoreDefaultInput();
            return GameActionResult.success();
        }

        // FAILURE CHECKS

        // Timeout: Too many ticks (5 seconds = 100 ticks)
        if (ticksRunning > 100) {
            context.restoreDefaultInput();
            return GameActionResult.failure("Movement timeout - took too long");
        }

        // Stuck detection: No progress for 1 second (20 ticks)
        if (ticksRunning > 20 && Math.abs(currentDistance - initialDistance) < 0.1) {
            context.restoreDefaultInput();
            return GameActionResult.failure("Player appears stuck - no progress made");
        }

        // Going wrong way: Moved significantly away from target
        if (currentDistance > initialDistance + 1.0) {
            context.restoreDefaultInput();
            return GameActionResult.failure("Moving away from target - possible collision");
        }

        // Still moving towards target
        return GameActionResult.failure("Moving to target - distance: " + String.format("%.3f", currentDistance));
    }

    private double calculateDistance(Vec3d pos1, Vec3d pos2) {
        double dx = pos1.x - pos2.x;
        double dz = pos1.z - pos2.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private String formatPosition(Vec3d pos) {
        return String.format("(%.2f, %.2f, %.2f)", pos.x, pos.y, pos.z);
    }
}
