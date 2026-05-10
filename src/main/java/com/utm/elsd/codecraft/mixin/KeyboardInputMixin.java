package com.utm.elsd.codecraft.mixin;

import com.utm.elsd.codecraft.implementation.movement.input.InputOverride;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        boolean forward = this.playerInput.forward() || InputOverride.INSTANCE.isForced(InputOverride.Key.FORWARD);
        boolean backward = this.playerInput.backward() || InputOverride.INSTANCE.isForced(InputOverride.Key.BACK);
        boolean left = this.playerInput.left() || InputOverride.INSTANCE.isForced(InputOverride.Key.LEFT);
        boolean right = this.playerInput.right() || InputOverride.INSTANCE.isForced(InputOverride.Key.RIGHT);
        boolean jump = this.playerInput.jump() || InputOverride.INSTANCE.isForced(InputOverride.Key.JUMP);
        boolean sneak = this.playerInput.sneak() || InputOverride.INSTANCE.isForced(InputOverride.Key.SNEAK);

        this.playerInput = new PlayerInput(
                forward,
                backward,
                left,
                right,
                jump,
                sneak,
                this.playerInput.sprint()
        );

        this.movementVector = new Vec2f(axis(left, right), axis(forward, backward)).normalize();
    }

    private static float axis(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        }
        return positive ? 1.0F : -1.0F;
    }
}