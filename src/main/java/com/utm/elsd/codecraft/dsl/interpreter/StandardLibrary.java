package com.utm.elsd.codecraft.dsl.interpreter;

import com.utm.elsd.codecraft.implementation.inventory.atoms.CloseInventoryAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.DropItemAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.MoveItemAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.OpenInventoryAction;
import com.utm.elsd.codecraft.implementation.inventory.atoms.ToolBarAction;
import com.utm.elsd.codecraft.implementation.inventory.helper.InventoryHelper;
import com.utm.elsd.codecraft.implementation.misc.WaitTicksAction;
import com.utm.elsd.codecraft.implementation.misc.PrintAction;
import com.utm.elsd.codecraft.implementation.movement.atoms.MoveForwardAction;
import com.utm.elsd.codecraft.implementation.player.atoms.TurnAction;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Set;

/**
 * Default function bindings for the DSL.
 *
 * This class keeps Minecraft-specific execution logic out of the interpreter core.
 */
public final class StandardLibrary {
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

    private StandardLibrary() {
    }

    public static void install(Interpreter.Builder builder) {
        builder
                .registerFunction("log", ctx -> {
                    ctx.log(joinArguments(ctx));
                    return Value.nullValue();
                })
                .registerFunction("print", ctx -> {
                    ctx.emit(new PrintAction(joinArguments(ctx)));
                    return Value.nullValue();
                })
                .registerFunction("tool_bar", ctx -> {
                    ctx.requireArity("tool_bar", 1);
                    ctx.emit(new ToolBarAction(ctx.argument(0).asInt()));
                    return Value.nullValue();
                })
                .registerFunction("move_forward", ctx -> {
                    ctx.requireArity("move_forward", 1);
                    ctx.emit(new MoveForwardAction(ctx.argument(0).asInt()));
                    return Value.nullValue();
                })
                .registerFunction("wait", ctx -> {
                    ctx.requireArity("wait", 1);
                    ctx.emit(new WaitTicksAction(ctx.argument(0).asInt()));
                    return Value.nullValue();
                })
                .registerFunction("wait_ticks", ctx -> {
                    ctx.requireArity("wait_ticks", 1);
                    ctx.emit(new WaitTicksAction(ctx.argument(0).asInt()));
                    return Value.nullValue();
                })
                .registerFunction("open_inventory", ctx -> {
                    ctx.emit(new OpenInventoryAction());
                    return Value.nullValue();
                })
                .registerFunction("close_inventory", ctx -> {
                    ctx.emit(new CloseInventoryAction());
                    return Value.nullValue();
                })
                .registerFunction("drop_item", ctx -> {
                    ctx.requireArity("drop_item", 2);
                    ctx.emit(new DropItemAction(ctx.argument(0).asInt(), ctx.argument(1).asInt()));
                    return Value.nullValue();
                })
                .registerFunction("move_item", ctx -> {
                    ctx.requireArity("move_item", 4);
                    ctx.emit(new MoveItemAction(
                            ctx.argument(0).asInt(),
                            ctx.argument(1).asInt(),
                            ctx.argument(2).asInt(),
                            ctx.argument(3).asInt()));
                    return Value.nullValue();
                })
                .registerFunction("item_at", ctx -> {
                    ctx.requireArity("item_at", 2);
                    if (ctx.minecraftContext() == null || ctx.minecraftContext().player() == null) {
                        return Value.nullValue();
                    }

                    int row = ctx.argument(0).asInt();
                    int col = ctx.argument(1).asInt();
                    if (!InventoryHelper.isValidSlot(row, col)) {
                        throw new InterpreterException("item_at received an invalid slot: row=" + row + ", col=" + col);
                    }

                    int screenSlot = InventoryHelper.calculateScreenSlot(row, col);
                    var stack = ctx.minecraftContext().player().playerScreenHandler.getSlot(screenSlot).getStack();
                    if (stack.isEmpty()) {
                        return Value.nullValue();
                    }

                    return Value.of("items." + Registries.ITEM.getId(stack.getItem()).getPath());
                })
                .registerFunction("is_empty", ctx -> {
                    ctx.requireArity("is_empty", 1);
                    return Value.of(!ctx.argument(0).isTruthy());
                })
                .registerFunction("is_edible", ctx -> {
                    ctx.requireArity("is_edible", 1);
                    String symbol = ctx.argument(0).asString();
                    if (!symbol.startsWith("items.")) {
                        return Value.of(false);
                    }

                    Identifier identifier = Identifier.tryParse("minecraft:" + symbol.substring("items.".length()));
                    if (identifier == null) {
                        return Value.of(false);
                    }

                    String path = Registries.ITEM.get(identifier).toString();
                    if (path == null) {
                        return Value.of(false);
                    }

                    path = Registries.ITEM.getId(Registries.ITEM.get(identifier)).getPath();
                    return Value.of(isLikelyEdible(path));
                })
                .registerFunction("is_item", ctx -> {
                    ctx.requireArity("is_item", 1);
                    return Value.of(isKnownItemSymbol(ctx.argument(0).asString()));
                })
                .registerFunction("use", ctx -> {
                    ctx.requireArity("use", 0);
                    ctx.emit(new com.utm.elsd.codecraft.implementation.player.atoms.UseAction());
                    return Value.nullValue();
                })
                .registerFunction("break", ctx -> {
                    ctx.requireArity("break", 0);
                    ctx.emit(new com.utm.elsd.codecraft.implementation.player.atoms.BreakAction());
                    return Value.nullValue();
                })
                .registerFunction("turn_left", ctx -> {
                    ctx.requireArity("turn_left", 0);
                    ctx.emit(new TurnAction(-90));
                    return Value.nullValue();
                })
                .registerFunction("turn_right", ctx -> {
                    ctx.requireArity("turn_right", 0);
                    ctx.emit(new TurnAction(90));
                    return Value.nullValue();
                })
                .registerFunction("center", ctx -> {
                    ctx.requireArity("center", 0);
                    ctx.emit(new TurnAction(0));
                    return Value.nullValue();
                });
    }

    public static SpecialObjectResolver defaultSpecialObjectResolver() {
        return (object, field, context) -> {
            if (object == null || field == null) {
                return Value.nullValue();
            }

            if ("items".equals(object)) {
                return Value.of("items." + field);
            }

            if ("state".equals(object) && context != null && context.minecraftContext() != null && context.minecraftContext().player() != null) {
                return switch (field) {
                    case "hunger", "food" -> Value.of(context.minecraftContext().player().getHungerManager().getFoodLevel());
                    case "health" -> Value.of(context.minecraftContext().player().getHealth());
                    case "x" -> Value.of(context.minecraftContext().position().x);
                    case "y" -> Value.of(context.minecraftContext().position().y);
                    case "z" -> Value.of(context.minecraftContext().position().z);
                    case "inventory_open" -> Value.of(context.minecraftContext().isInventoryScreenOpen());
                    default -> Value.of(object + "." + field);
                };
            }

            return Value.of(object + "." + field);
        };
    }

    private static String joinArguments(ExecutionContext context) {
        if (context.arguments().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < context.arguments().size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(context.arguments().get(i).asString());
        }
        return builder.toString();
    }

    private static boolean isLikelyEdible(String itemPath) {
        if (itemPath == null || itemPath.isBlank()) {
            return false;
        }
        return EDIBLE_ITEM_NAMES.contains(itemPath);
    }

    private static boolean isKnownItemSymbol(String symbol) {
        if (symbol == null || !symbol.startsWith("items.")) {
            return false;
        }

        Identifier identifier = Identifier.tryParse("minecraft:" + symbol.substring("items.".length()));
        if (identifier == null) {
            return false;
        }

        return Objects.equals(Registries.ITEM.getId(Registries.ITEM.get(identifier)), identifier);
    }
}



