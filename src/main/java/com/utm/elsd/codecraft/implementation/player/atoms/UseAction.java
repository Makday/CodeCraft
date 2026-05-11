package com.utm.elsd.codecraft.implementation.player.atoms;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import java.util.Set;

/**
 * Performs a right-click (use) with the currently held item.
 *
 * This implementation mirrors vanilla right-click flow by first checking the
 * current crosshair target (block/entity) and only falling back to item use.
 */
public class UseAction implements Action {
    private boolean consuming = false;
    private Item initialItem = null;
    private int initialCount = -1;
    private int initialHunger = -1;
    private int consumingTicks = 0;
    private static final int MAX_CONSUME_TICKS = 100; // safety timeout
    private boolean useKeyPressed = false;
    
    // Set of known edible items (food)
    private static final Set<String> EDIBLE_ITEM_NAMES = Set.of(
            "apple",
            "baked_potato",
            "beetroot",
            "beetroot_soup",
            "bread",
            "cake",
            "carrot",
            "chorus_fruit",
            "cookie",
            "dried_kelp",
            "enchanted_golden_apple",
            "golden_apple",
            "glow_berries",
            "honey_bottle",
            "melon_slice",
            "mushroom_stew",
            "potato",
            "pufferfish",
            "pumpkin_pie",
            "rabbit_stew",
            "red_mushroom",
            "spider_eye",
            "sweet_berries",
            "tropical_fish",
            "rotten_flesh",
            "cooked_beef",
            "cooked_chicken",
            "cooked_cod",
            "cooked_mutton",
            "cooked_porkchop",
            "cooked_rabbit",
            "cooked_salmon",
            "cooked_steak"
    );

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        if (ctx == null) return ActionStatus.DONE;

        MinecraftClient client = ctx.client();
        ClientPlayerEntity player = ctx.player();
        if (client == null || player == null) return ActionStatus.DONE;

        if (client.interactionManager == null) {
            return ActionStatus.DONE;
        }

        try {
            // If we are currently consuming (holding right-click to eat), wait until the use completes
            if (consuming) {
                consumingTicks++;
                // Keep the use key pressed to simulate continuous holding
                if (!useKeyPressed) {
                    client.options.useKey.setPressed(true);
                    useKeyPressed = true;
                }

                ItemStack cur = player.getMainHandStack();
                int curCount = cur == null ? 0 : cur.getCount();
                int curHunger = player.getHungerManager().getFoodLevel();

                boolean consumed = false;
                if (cur == null || cur.isEmpty()) consumed = true;
                else if (initialItem == null) consumed = true;
                else if (!cur.getItem().equals(initialItem)) consumed = true;
                else if (curCount < initialCount) consumed = true;
                else if (curHunger > initialHunger) consumed = true;

                if (consumed || consumingTicks > MAX_CONSUME_TICKS) {
                    // Release the use key now that use is complete
                    if (useKeyPressed) {
                        client.options.useKey.setPressed(false);
                        useKeyPressed = false;
                    }
                    consuming = false;
                    initialItem = null;
                    initialCount = -1;
                    initialHunger = -1;
                    consumingTicks = 0;
                    // ensure animation
                    player.swingHand(Hand.MAIN_HAND);
                    return ActionStatus.DONE;
                }

                return ActionStatus.RUNNING;
            }
            // Mirror vanilla: try block/entity first, then item. If item use starts a continuous
            // use (e.g. eating), the client will report isUsingItem() and we should wait until
            // the use completes.
            boolean handled = false;
            HitResult hitResult = client.crosshairTarget;

            if (hitResult != null) {
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    ActionResult result = client.interactionManager.interactBlock(
                            player,
                            Hand.MAIN_HAND,
                            (BlockHitResult) hitResult
                    );
                    handled = result != ActionResult.PASS;
                } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hitResult;
                    Entity entity = entityHit.getEntity();

                    ActionResult result = client.interactionManager.interactEntityAtLocation(
                            player,
                            entity,
                            entityHit,
                            Hand.MAIN_HAND
                    );
                    if (result == ActionResult.PASS) {
                        result = client.interactionManager.interactEntity(player, entity, Hand.MAIN_HAND);
                    }
                    handled = result != ActionResult.PASS;
                }
            }

            if (!handled) {
                ActionResult itemResult = client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                // If the interaction manager accepted the use (itemResult != PASS), check if it's an edible item
                // to enter consuming mode so we wait until the eating completes.
                if (itemResult != ActionResult.PASS) {
                    ItemStack stack = player.getMainHandStack();
                    Item item = stack == null ? null : stack.getItem();
                    
                    // Only enter consuming mode for edible items (food)
                    if (item != null && isEdible(item)) {
                        initialItem = item;
                        initialCount = stack.getCount();
                        initialHunger = player.getHungerManager().getFoodLevel();
                        consumingTicks = 0;
                        consuming = true;
                        return ActionStatus.RUNNING;
                    }
                }
            }
        } catch (Throwable ignored) {
            // Keep the interpreter resilient: if interaction fails, do not crash.
        }

        return ActionStatus.DONE;
    }

    /**
     * Check if an item is edible by checking its registry ID against known food items.
     */
    private boolean isEdible(Item item) {
        String itemPath = Registries.ITEM.getId(item).getPath();
        return EDIBLE_ITEM_NAMES.contains(itemPath);
    }
}




