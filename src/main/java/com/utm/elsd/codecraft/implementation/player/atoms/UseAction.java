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
 * Can be called with no arguments (uses crosshair target) or with three arguments
 * representing relative block position from the player's perspective:
 *   - (0, 0, 1) = 1 block forward (where player is looking)
 *   - (1, 0, 0) = 1 block to the right
 *   - (-1, 0, 0) = 1 block to the left
 *   - (0, 1, 0) = 1 block up
 *   - (0, 0, -1) = 1 block backward
 *
 * Max distance is 4 blocks. Cannot use on the player's own position.
 *
 * For positional placement, prioritizes adjacent blocks in this order:
 * 1. Block below (most natural for ground placement)
 * 2. Block above
 * 3. Adjacent horizontal blocks
 *
 * This implementation mirrors vanilla right-click flow by first checking the
 * current crosshair target (block/entity) and only falling back to item use.
 */
public class UseAction implements Action {
    private final Integer relX;
    private final Integer relY;
    private final Integer relZ;
    private static final int MAX_DISTANCE = 4;
    
    private boolean consuming = false;
    private Item initialItem = null;
    private int initialCount = -1;
    private int initialHunger = -1;
    private int consumingTicks = 0;
    private static final int MAX_CONSUME_TICKS = 100; // safety timeout
    private boolean useKeyPressed = false;
    
    // Water bucket handling state
    private boolean usingWaterBucket = false;
    private float originalYaw = 0f;
    private float originalPitch = 0f;
    private boolean turnedToTarget = false;
    private int waterBucketTicks = 0;
    private static final int WATER_BUCKET_TIMEOUT = 40; // ticks to allow for turning + using
    
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

    /**
     * Use the item on the crosshair target (block/entity).
     */
    public UseAction() {
        this.relX = null;
        this.relY = null;
        this.relZ = null;
    }

    /**
     * Use the item on a block at a relative position from the player's legs.
     * 
     * @param relX relative X coordinate (normalized: -4 to 4)
     * @param relY relative Y coordinate (normalized: -4 to 4)
     * @param relZ relative Z coordinate (normalized: -4 to 4)
     * @throws IllegalArgumentException if position is too far or on the player
     */
    public UseAction(int relX, int relY, int relZ) {
        // Validate distance
        int maxDist = Math.max(Math.max(Math.abs(relX), Math.abs(relY)), Math.abs(relZ));
        if (maxDist > MAX_DISTANCE) {
            throw new IllegalArgumentException("Position too far: max distance is " + MAX_DISTANCE + " blocks");
        }
        // Validate not on player
        if (relX == 0 && relY == 0 && relZ == 0) {
            throw new IllegalArgumentException("Cannot use on the player's own position");
        }
        this.relX = relX;
        this.relY = relY;
        this.relZ = relZ;
    }

    private net.minecraft.util.math.BlockPos getTargetBlockPos(MinecraftContext ctx) {
        ClientPlayerEntity player = ctx.player();
        net.minecraft.util.math.BlockPos origin = player.getBlockPos();
        float snappedYaw = TurnAction.computeNearestCardinalYaw(player.getYaw());

        int dx;
        int dz;
        switch ((int) snappedYaw) {
            case 0 -> {          // South
                dx = -relX;
                dz = relZ;
            }
            case 90 -> {         // West
                dx = -relZ;
                dz = -relX;
            }
            case 180 -> {        // North
                dx = relX;
                dz = -relZ;
            }
            case -90 -> {        // East
                dx = relZ;
                dz = relX;
            }
            default -> {
                dx = -relX;
                dz = relZ;
            }
        }

        return origin.add(dx, relY, dz);
    }

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

            // If relative coordinates are provided, use that block position
            if (relX != null && relY != null && relZ != null) {
                net.minecraft.util.math.BlockPos targetPos = getTargetBlockPos(ctx);
                net.minecraft.world.World world = ctx.world();
                
                if (world == null) return ActionStatus.DONE;

                // Special handling for water bucket with turning and facing restoration
                if (isHoldingWaterBucket(player)) {
                    // Water bucket placement by looking at target
                    if (!usingWaterBucket) {
                        // Start water bucket sequence: store original facing
                        usingWaterBucket = true;
                        originalYaw = player.getYaw();
                        originalPitch = player.getPitch();
                        turnedToTarget = false;
                        waterBucketTicks = 0;
                        return ActionStatus.RUNNING;
                    }

                    waterBucketTicks++;

                    // Calculate angles to face target position
                    net.minecraft.util.math.Vec3d targetCenter = new net.minecraft.util.math.Vec3d(
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.5,
                        targetPos.getZ() + 0.5
                    );
                    float[] angles = calculateLookAtAngles(player, targetCenter);
                    float targetYaw = angles[0];
                    float targetPitch = angles[1];

                    // Turn towards target
                    if (!turnedToTarget) {
                        float currentYaw = player.getYaw();
                        float currentPitch = player.getPitch();
                        
                        // Set new rotation
                        player.setYaw(targetYaw);
                        player.setPitch(targetPitch);

                        // Check if close enough to target yaw/pitch (using current values before next tick)
                        float yawDiff = Math.abs(targetYaw - currentYaw);
                        if (yawDiff > 180) yawDiff = 360 - yawDiff;
                        float pitchDiff = Math.abs(targetPitch - currentPitch);

                        if (yawDiff < 5 && pitchDiff < 5) {
                            turnedToTarget = true;
                        }
                    }

                    // Once turned, use the water bucket
                    if (turnedToTarget) {
                        ActionResult result = client.interactionManager.interactItem(player, Hand.MAIN_HAND);

                        // Always restore facing and finish (whether water was used or not)
                        player.setYaw(originalYaw);
                        player.setPitch(originalPitch);
                        usingWaterBucket = false;
                        turnedToTarget = false;
                        return ActionStatus.DONE;
                    }

                    // Safety timeout
                    if (waterBucketTicks > WATER_BUCKET_TIMEOUT) {
                        player.setYaw(originalYaw);
                        player.setPitch(originalPitch);
                        usingWaterBucket = false;
                        turnedToTarget = false;
                        return ActionStatus.DONE;
                    }

                    return ActionStatus.RUNNING;
                }

                // For block placement, we need to click on an adjacent block's face
                // Priority order: below, above, then other adjacent blocks
                net.minecraft.util.math.BlockPos adjacentPos = null;
                net.minecraft.util.math.Direction clickFace = null;

                // 1. Try block below (most natural for placement)
                net.minecraft.util.math.BlockPos below = targetPos.down();
                if (!world.getBlockState(below).isAir()) {
                    adjacentPos = below;
                    clickFace = net.minecraft.util.math.Direction.UP;
                }

                // 2. Try block above
                if (adjacentPos == null) {
                    net.minecraft.util.math.BlockPos above = targetPos.up();
                    if (!world.getBlockState(above).isAir()) {
                        adjacentPos = above;
                        clickFace = net.minecraft.util.math.Direction.DOWN;
                    }
                }

                // 3. Try other adjacent blocks
                if (adjacentPos == null) {
                    net.minecraft.util.math.Direction[] directions = net.minecraft.util.math.Direction.values();
                    for (net.minecraft.util.math.Direction dir : directions) {
                        // Skip UP and DOWN since we already tried those
                        if (dir == net.minecraft.util.math.Direction.UP || dir == net.minecraft.util.math.Direction.DOWN) {
                            continue;
                        }

                        net.minecraft.util.math.BlockPos adjacent = targetPos.offset(dir);
                        if (!world.getBlockState(adjacent).isAir()) {
                            adjacentPos = adjacent;
                            clickFace = dir.getOpposite();
                            break;
                        }
                    }
                }

                // If no adjacent block found, target is surrounded by air - can't place
                if (adjacentPos == null) {
                    return ActionStatus.DONE;
                }

                // Create a hit result on the adjacent block's face
                net.minecraft.util.hit.BlockHitResult blockHit = new net.minecraft.util.hit.BlockHitResult(
                    new net.minecraft.util.math.Vec3d(
                        adjacentPos.getX() + 0.5,
                        adjacentPos.getY() + 0.5,
                        adjacentPos.getZ() + 0.5
                    ),
                    clickFace,
                    adjacentPos,
                    false
                );

                ActionResult result = client.interactionManager.interactBlock(player, Hand.MAIN_HAND, blockHit);

                // If not handled by block placement, try item use (for things like chests)
                if (result == ActionResult.PASS) {
                    // Try to interact with the target block directly for things like opening chests
                    net.minecraft.block.BlockState targetState = world.getBlockState(targetPos);
                    if (!targetState.isAir()) {
                        net.minecraft.util.hit.BlockHitResult directHit = new net.minecraft.util.hit.BlockHitResult(
                            new net.minecraft.util.math.Vec3d(
                                targetPos.getX() + 0.5,
                                targetPos.getY() + 0.5,
                                targetPos.getZ() + 0.5
                            ),
                            net.minecraft.util.math.Direction.UP,
                            targetPos,
                            false
                        );
                        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, directHit);
                    }
                }

                return ActionStatus.DONE;
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

    /**
     * Check if the player is holding a water bucket or empty bucket
     */
    private boolean isHoldingWaterBucket(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack == null || stack.isEmpty()) return false;
        String itemPath = Registries.ITEM.getId(stack.getItem()).getPath();
        return itemPath.equals("water_bucket") || itemPath.equals("bucket");
    }

    /**
     * Check if the player has line of sight to the target position by checking distance
     */
    private boolean canSeeLOS(MinecraftContext ctx, net.minecraft.util.math.BlockPos targetPos) {
        ClientPlayerEntity player = ctx.player();
        if (player == null) return false;

        // Simple line-of-sight check: if target is within about 4 blocks and we can see it
        double dx = targetPos.getX() + 0.5 - player.getX();
        double dy = targetPos.getY() + 0.5 - (player.getY() + player.getEyeHeight(player.getPose()));
        double dz = targetPos.getZ() + 0.5 - player.getZ();

        // Check if target is within 4 blocks
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq <= 16.0; // 4 blocks squared
    }

    /**
     * Calculate yaw and pitch to face the target position
     */
    private float[] calculateLookAtAngles(ClientPlayerEntity player, net.minecraft.util.math.Vec3d targetPos) {
        net.minecraft.util.math.Vec3d eyePos = new net.minecraft.util.math.Vec3d(
            player.getX(),
            player.getY() + player.getEyeHeight(player.getPose()),
            player.getZ()
        );

        net.minecraft.util.math.Vec3d diff = targetPos.subtract(eyePos);
        double distHorizontal = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distHorizontal));

        return new float[]{yaw, pitch};
    }
}