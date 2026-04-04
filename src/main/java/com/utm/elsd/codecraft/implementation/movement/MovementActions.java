package com.utm.elsd.codecraft.implementation.movement;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.implementation.movement.atoms.MoveForwardAction;

/**
 * Movement-related actions for the DSL.
 */
public class MovementActions {

    /**
     * Creates an action to move the player forward by the specified number of blocks.
     * Movement happens gradually over multiple game ticks.
     *
     * @param blocks The number of blocks to move forward
     * @return A GameAction that will execute the movement
     */
    public static GameAction moveForward(int blocks) {
        return new MoveForwardAction(blocks);
    }
}

