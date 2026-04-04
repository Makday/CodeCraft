package com.utm.elsd.codecraft.api;

import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Represents a single atomic action that can be executed in the Minecraft game world.
 * Actions are the building blocks of DSL scripts and encapsulate specific game operations.
 */
public interface GameAction {

    /**
     * Executes this action using the provided Minecraft context.
     *
     * @param context The current Minecraft game state and access to game objects
     * @return The result of the action execution
     */
    GameActionResult execute(MinecraftContext context);
}
