package com.utm.elsd.codecraft;

import com.utm.elsd.codecraft.api.GameActionExecutor;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.InventoryActions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
                    .then(ClientCommandManager.literal("move")
                            .then(ClientCommandManager.argument("fromRow", IntegerArgumentType.integer(0, 3))
                                    .then(ClientCommandManager.argument("fromCol", IntegerArgumentType.integer(0, 8))
                                            .then(ClientCommandManager.argument("toRow", IntegerArgumentType.integer(0, 3))
                                                    .then(ClientCommandManager.argument("toCol", IntegerArgumentType.integer(0, 8))
                                                            .executes(context -> {
                                                                int fromRow = IntegerArgumentType.getInteger(context, "fromRow");
                                                                int fromCol = IntegerArgumentType.getInteger(context, "fromCol");
                                                                int toRow = IntegerArgumentType.getInteger(context, "toRow");
                                                                int toCol = IntegerArgumentType.getInteger(context, "toCol");
                                                                var result = actionExecutor.executeAction("move_item", fromRow, fromCol, toRow, toCol);
                                                                context.getSource().sendFeedback(
                                                                        Text.literal("move_item(" + fromRow + ", " + fromCol + ", " + toRow + ", " + toCol + "): " + result.toString())
                                                                );
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )
                            )
                    )
                    .then(ClientCommandManager.literal("open")
                            .executes(context -> {
                                var result = actionExecutor.executeAction("open_inventory");
                                context.getSource().sendFeedback(
                                        Text.literal("open_inventory(): " + result.toString())
                                );
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("close")
                            .executes(context -> {
                                var result = actionExecutor.executeAction("close_inventory");
                                context.getSource().sendFeedback(
                                        Text.literal("close_inventory(): " + result.toString())
                                );
                                return 1;
                            })
                    )
            );

            // /testfullinventory
            dispatcher.register(ClientCommandManager.literal("testfullinventory")
                    .executes(context -> {
                        Thread thread = new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }

                            net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                                var openResult = actionExecutor.executeAction("open_inventory");
                                context.getSource().sendFeedback(
                                        Text.literal("open_inventory(): " + openResult.toString())
                                );
                            });

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }

                            net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                                var closeResult = actionExecutor.executeAction("close_inventory");
                                context.getSource().sendFeedback(
                                        Text.literal("close_inventory(): " + closeResult.toString())
                                );
                            });
                        });
                        thread.setDaemon(true);
                        thread.start();

                        return 1;
                    })
            );

        });
    }
}