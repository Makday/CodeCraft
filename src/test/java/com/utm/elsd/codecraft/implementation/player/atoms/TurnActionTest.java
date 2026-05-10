package com.utm.elsd.codecraft.implementation.player.atoms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnActionTest {

    @Test
    void turnsRightToClockwiseStableBoundaryWhenBetweenCardinals() {
        // Between South (0) and West (90) -> right snaps to South.
        // snapping inverted: right should now snap to the opposite side
        assertEquals(90.0f, TurnAction.computeStableTargetYaw(45.0f, 90));
    }

    @Test
    void turnsLeftToCounterClockwiseStableBoundaryWhenBetweenCardinals() {
        // Between South (0) and West (90) -> left snaps to West.
        // snapping inverted: left should now snap to the opposite side
        assertEquals(0.0f, TurnAction.computeStableTargetYaw(45.0f, -90));
    }

    @Test
    void turnsRightFromExactCardinalToNextCardinal() {
        // West is 90; right should be North (180).
        assertEquals(180.0f, TurnAction.computeStableTargetYaw(90.0f, 90));
    }

    @Test
    void turnsLeftFromExactCardinalToPreviousCardinal() {
        // West is 90; left should be South (0).
        assertEquals(0.0f, TurnAction.computeStableTargetYaw(90.0f, -90));
    }
}


