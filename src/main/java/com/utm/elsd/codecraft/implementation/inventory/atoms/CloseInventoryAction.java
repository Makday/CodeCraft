package com.utm.elsd.codecraft.implementation.inventory.atoms;

import com.utm.elsd.codecraft.api.GameAction;
import com.utm.elsd.codecraft.api.GameActionResult;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class CloseInventoryAction implements GameAction {
    private boolean started = false;

    @Override
    public GameActionResult<Void> execute(MinecraftContext context) {
        if (!context.isAvailable()) {
            return GameActionResult.failure("Minecraft context not available");
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // If inventory is already closed, say it
        if (!(client.currentScreen instanceof InventoryScreen)) {
            return GameActionResult.failure("Inventory is not open");
        }

        // Close the screen on first execution
        if (!started) {
            started = true;
            client.send(() -> client.setScreen(null));
            return GameActionResult.failure("Closing inventory...");
        }

        // Second tick: verify it actually closed
        if (!(client.currentScreen instanceof InventoryScreen)) {
            return GameActionResult.success();
        }

        return GameActionResult.failure("Failed to close inventory");
    }
}