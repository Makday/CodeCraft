package com.utm.elsd.codecraft;

import com.utm.elsd.codecraft.api.GameActionExecutor;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class CodeCraftClient implements ClientModInitializer {
    // game action executor for testing DSL actions
    private final GameActionExecutor actionExecutor = new GameActionExecutor(new MinecraftContext());

    @Override
    public void onInitializeClient() {
        // test commands for the new action system
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("testmove")
                    .then(ClientCommandManager.literal("forward")
                            .executes(context -> {
                                var result = actionExecutor.executeAction("move_forward",1);
                                context.getSource().sendFeedback(
                                        Text.literal("move_forward(1): " + result.toString())
                                );
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("toolbar")
                            .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(0, 8))
                                    .executes(context -> {
                                        int slot = IntegerArgumentType.getInteger(context, "slot");
                                        var result = actionExecutor.executeAction("tool_bar", slot);
                                        context.getSource().sendFeedback(
                                                Text.literal("tool_bar(" + slot + "): " + result.toString())
                                        );
                                        return 1;
                                    })
                            )
                    )
            );
        });
    }
}