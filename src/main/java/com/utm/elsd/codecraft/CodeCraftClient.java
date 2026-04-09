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
                                var result = actionExecutor.executeAction("move_forward", 1);
                                context.getSource().sendFeedback(
                                        Text.literal("move_forward(1): " + result.toString())
                                );
                                return 1;
                            })
                    )
            );

            dispatcher.register(ClientCommandManager.literal("testinventory")
                    .then(ClientCommandManager.literal("drop")
                            .then(ClientCommandManager.argument("row", IntegerArgumentType.integer(0, 3))
                                    .then(ClientCommandManager.argument("col", IntegerArgumentType.integer(0, 8))
                                            .executes(context -> {
                                                int row = IntegerArgumentType.getInteger(context, "row");
                                                int col = IntegerArgumentType.getInteger(context, "col");
                                                var result = actionExecutor.executeAction("drop_item", row, col);
                                                context.getSource().sendFeedback(
                                                        Text.literal("drop_item(" + row + ", " + col + "): " + result.toString())
                                                );
                                                return 1;
                                            })
                                    )
                            )
                    )
            );
        });
    }
}