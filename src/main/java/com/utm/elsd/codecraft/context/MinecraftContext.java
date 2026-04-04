package com.utm.elsd.codecraft.context;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MinecraftContext {

    public ClientPlayerEntity player() {
        return MinecraftClient.getInstance().player;
    }

    public World world() {
        ClientPlayerEntity player = player();
        return player != null ? player.getWorld() : null;
    }

    public BlockPos blockPos() {
        ClientPlayerEntity player = player();
        return player != null ? player.getBlockPos() : null;
    }

    public Vec3d position() {
        ClientPlayerEntity player = player();
        return player != null ? player.getPos() : Vec3d.ZERO;
    }

    public Vec3d velocity() {
        ClientPlayerEntity player = player();
        return player != null ? player.getVelocity() : Vec3d.ZERO;
    }

    public void setInput(Input input) {
        ClientPlayerEntity player = player();
        if (player != null) {
            player.input = input;
        }
    }

    public void restoreDefaultInput() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.input = new KeyboardInput(client.options);
        }
    }

    public Vec3d facingDirection() {
        ClientPlayerEntity player = player();
        return player != null ? Vec3d.fromPolar(0, player.getYaw()) : Vec3d.ZERO;
    }

    public boolean isAvailable() {
        return player() != null && world() != null;
    }
}
