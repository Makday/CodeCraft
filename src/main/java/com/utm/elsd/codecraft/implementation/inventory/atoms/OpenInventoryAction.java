package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class OpenInventoryAction implements GameAction {
    private boolean started = false;

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // If inventory is already open, succeed immediately
        if (client.currentScreen instanceof InventoryScreen) {
            return GameActionResult.success();
        }

        // Open the inventory on first execution
        if (!started) {
            started = true;
            client.send(() -> client.setScreen(
                    new InventoryScreen(client.player)
            ));
            return GameActionResult.failure("Opening inventory...");
        }

        // Second tick: verify it actually opened
        if (client.currentScreen instanceof InventoryScreen) {
            return GameActionResult.success();
        }

        return GameActionResult.failure("Failed to open inventory");
    }
}