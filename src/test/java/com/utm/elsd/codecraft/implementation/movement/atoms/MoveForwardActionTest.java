package com.utm.elsd.codecraft.implementation.movement.atoms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveForwardActionTest {

    @Test
    void computesTargetCenterForSouthFacingYaw() {
        double[] target = MoveForwardAction.computeTargetCenter(0.1, 0.1, 0.0f, 1);
        assertEquals(0.5, target[0], 1.0e-6);
        assertEquals(1.5, target[1], 1.0e-6);
    }

    @Test
    void computesTargetCenterForWestFacingYaw() {
        double[] target = MoveForwardAction.computeTargetCenter(10.2, 5.8, 90.0f, 2);
        assertEquals(8.5, target[0], 1.0e-6);
        assertEquals(5.5, target[1], 1.0e-6);
    }

    @Test
    void computesTargetCenterForNorthFacingYaw() {
        double[] target = MoveForwardAction.computeTargetCenter(-2.7, 4.3, 180.0f, 3);
        assertEquals(-2.5, target[0], 1.0e-6);
        assertEquals(1.5, target[1], 1.0e-6);
    }

    @Test
    void allowsSmallStepChangesTypicalForWalkableBlocks() {
        assertEquals(true, MoveForwardAction.isStepAllowedForTest(64.0, 64.5));
        assertEquals(true, MoveForwardAction.isStepAllowedForTest(64.9375, 65.0));
    }

    @Test
    void rejectsLargeStepChangesThatWouldRequireJumping() {
        assertEquals(false, MoveForwardAction.isStepAllowedForTest(64.0, 65.0));
        assertEquals(false, MoveForwardAction.isStepAllowedForTest(65.0, 64.0 - 0.7));
    }
}


