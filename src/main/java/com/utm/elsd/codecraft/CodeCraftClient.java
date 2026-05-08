package com.utm.elsd.codecraft;

import com.utm.elsd.codecraft.api.ActionRunner;
import com.utm.elsd.codecraft.api.ActionSequence;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.implementation.inventory.atoms.CloseInventoryAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.DropItemAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.MoveItemAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.OpenInventoryAction;
import com.utm.elsd.codecraft.implementation.misc.WaitTicksAction;
import com.utm.elsd.codecraft.implementation.movement.atoms.MoveForwardAction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class CodeCraftClient implements ClientModInitializer {
    private ActionRunner runner;

    @Override
    public void onInitializeClient() {
        runner = new ActionRunner(new MinecraftContext());

        ClientTickEvents.END_CLIENT_TICK.register(client -> runner.tick());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(ClientCommandManager.literal("testmove")
                    .then(ClientCommandManager.literal("forward")
                            .then(ClientCommandManager.argument("blocks", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        int blocks = IntegerArgumentType.getInteger(ctx, "blocks");
                                        runner.run(ActionSequence.start(new MoveForwardAction(blocks)));
                                        return 1;
                                    })
                            )
                    )
            );

            dispatcher.register(ClientCommandManager.literal("testinventory")
                    .then(ClientCommandManager.literal("open")
                            .executes(ctx -> {
                                runner.run(ActionSequence.start(new OpenInventoryAction()));
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("close")
                            .executes(ctx -> {
                                runner.run(ActionSequence.start(new CloseInventoryAction()));
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("drop")
                            .then(ClientCommandManager.argument("row", IntegerArgumentType.integer(0, 3))
                                    .then(ClientCommandManager.argument("col", IntegerArgumentType.integer(0, 8))
                                            .executes(ctx -> {
                                                int row = IntegerArgumentType.getInteger(ctx, "row");
                                                int col = IntegerArgumentType.getInteger(ctx, "col");
                                                runner.run(ActionSequence.start(new DropItemAction(row, col)));
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
                                                            .executes(ctx -> {
                                                                int fromRow = IntegerArgumentType.getInteger(ctx, "fromRow");
                                                                int fromCol = IntegerArgumentType.getInteger(ctx, "fromCol");
                                                                int toRow   = IntegerArgumentType.getInteger(ctx, "toRow");
                                                                int toCol   = IntegerArgumentType.getInteger(ctx, "toCol");
                                                                runner.run(ActionSequence.start(new MoveItemAction(fromRow, fromCol, toRow, toCol)));
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )
                            )
                    )
            );

            dispatcher.register(ClientCommandManager.literal("testfullinventory")
                    .executes(ctx -> {
                        runner.run(
                                ActionSequence
                                        .start(new OpenInventoryAction())
                                        .then(new WaitTicksAction(60))
                                        .then(new CloseInventoryAction())
                        );
                        return 1;
                    })
            );

        });
    }
}