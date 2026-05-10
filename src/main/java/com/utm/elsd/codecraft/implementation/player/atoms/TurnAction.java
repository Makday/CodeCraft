package com.utm.elsd.codecraft.implementation.player.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Turns the player horizontally to a stable cardinal direction.
 *
 * Degrees are interpreted in quarter turns (multiples of 90):
 * - positive values turn right
 * - negative values turn left
 */
public class TurnAction implements Action {
    private static final float EPSILON = 1.0e-3f;

    private final int degrees;

    public TurnAction(int degrees) {
        if (degrees % 90 != 0) {
            throw new IllegalArgumentException("TurnAction degrees must be a multiple of 90, got: " + degrees);
        }
        this.degrees = degrees;
    }

    public int degrees() {
        return degrees;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (ctx == null || ctx.player() == null) {
            return ActionStatus.DONE;
        }

        float targetYaw = computeStableTargetYaw(ctx.player().getYaw(), degrees);
        ctx.player().setYaw(targetYaw);
        return ActionStatus.DONE;
    }

    static float computeStableTargetYaw(float currentYaw, int degrees) {
        if (degrees % 90 != 0) {
            throw new IllegalArgumentException("degrees must be a multiple of 90");
        }

        int quarterTurns = degrees / 90;
        if (quarterTurns == 0) {
            return toMinecraftYaw(normalizeYaw(currentYaw));
        }

        float normalized = normalizeYaw(currentYaw);
        boolean onCardinal = isOnCardinal(normalized);
        int targetIndex;

        if (quarterTurns > 0) {
            // when not exactly on a cardinal, choose the other side for "right" turns
            int clockwiseSide = onCardinal ? cardinalIndex(normalized) : (int) Math.ceil(normalized / 90.0f);
            int effectiveTurns = onCardinal ? quarterTurns : quarterTurns - 1;
            targetIndex = mod4(clockwiseSide + effectiveTurns);
        } else {
            // when not exactly on a cardinal, choose the other side for "left" turns
            int steps = -quarterTurns;
            int counterClockwiseSide = onCardinal
                    ? cardinalIndex(normalized)
                    : (int) Math.floor(normalized / 90.0f);
            int effectiveTurns = onCardinal ? steps : steps - 1;
            targetIndex = mod4(counterClockwiseSide - effectiveTurns);
        }

        return toMinecraftYaw(targetIndex * 90.0f);
    }

    private static float normalizeYaw(float yaw) {
        float normalized = yaw % 360.0f;
        if (normalized < 0.0f) {
            normalized += 360.0f;
        }
        return normalized;
    }

    private static boolean isOnCardinal(float normalizedYaw) {
        float remainder = normalizedYaw % 90.0f;
        return remainder < EPSILON || (90.0f - remainder) < EPSILON;
    }

    private static int cardinalIndex(float normalizedYaw) {
        return mod4(Math.round(normalizedYaw / 90.0f));
    }

    private static int mod4(int value) {
        int mod = value % 4;
        return mod < 0 ? mod + 4 : mod;
    }

    private static float toMinecraftYaw(float normalizedYaw) {
        return normalizedYaw > 180.0f ? normalizedYaw - 360.0f : normalizedYaw;
    }
}


