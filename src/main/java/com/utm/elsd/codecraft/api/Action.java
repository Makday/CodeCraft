package com.utm.elsd.codecraft.api;

import com.utm.elsd.codecraft.context.MinecraftContext;

/**
 * Represents a single atomic action that can be executed in the Minecraft game world.
 */
public interface Action {

    /**
     * Executes this action using the provided Minecraft context.
     *
     * @param ctx The current Minecraft game state and access to game objects
     * @return The result of the action execution
     */
    ActionStatus tick(MinecraftContext ctx);
}
