package com.utm.elsd.codecraft;

import com.utm.elsd.codecraft.api.GameActionExecutor;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.InventoryActions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

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

            // /testinventory open
            // /testinventory close
            dispatcher.register(ClientCommandManager.literal("testinventory")
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
                    .then(ClientCommandManager.literal("test")
                            .executes(context -> {
                                InventoryActions.openInventory().execute(new MinecraftContext());
                                for (int i = 0; i < 10000; i++) {
                                }
                                InventoryActions.closeInventory().execute(new MinecraftContext());

                                return 1;
                            })
                    )
            );

            // /testinventory
            dispatcher.register(ClientCommandManager.literal("testinventory")
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