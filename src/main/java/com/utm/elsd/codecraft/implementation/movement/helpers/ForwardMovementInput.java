package com.utm.elsd.codecraft.implementation.movement.helpers;

import net.minecraft.util.math.Vec2f;
import net.minecraft.client.input.Input;

/**
 * Custom input that forces forward movement.
 */
public class ForwardMovementInput extends Input {
    @Override
    public void tick() {
        // Set movement to forward only
        this.movementVector = this.getMovementInput();
    }

    @Override
    public boolean hasForwardMovement() {
        return true;
    }

    @Override
    public Vec2f getMovementInput() {
        // Straight forward movement
        return new Vec2f(0, 1);
    }
}
