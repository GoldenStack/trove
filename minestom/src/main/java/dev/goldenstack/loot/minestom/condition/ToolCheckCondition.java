package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.check.ItemCheck;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.type;

/**
 * Verifies that all provided contexts have a tool that passes the {@link #toolCheck()}.
 */
public record ToolCheckCondition(@NotNull ItemCheck toolCheck) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:match_tool";

    /**
     * A standard map-based converter for tool check conditions.
     */
    public static final @NotNull TypedLootConverter<ToolCheckCondition> CONVERTER =
            converter(ToolCheckCondition.class,
                    type(ItemCheck.class).name("toolCheck").nodePath("predicate").withDefault(ItemCheck.EMPTY)
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        ItemStack tool = context.get(LootContextKeys.TOOL);

        return tool == null || toolCheck.verify(context, tool);
    }
}
