package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.check.ItemCheck;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Field.field;

/**
 * Verifies that all provided contexts have a tool that passes the {@link #toolCheck()}.
 */
public record ToolCheckCondition(@NotNull ItemCheck toolCheck) implements LootCondition {

    /**
     * A standard map-based converter for tool check conditions.
     */
    public static final @NotNull KeyedLootConverter<ToolCheckCondition> CONVERTER =
            converter(ToolCheckCondition.class,
                    field(ItemCheck.class, ItemCheck.CONVERTER).name("toolCheck").nodePath("predicate").withDefault(ItemCheck.EMPTY)
            ).keyed("minecraft:match_tool");

    @Override
    public boolean verify(@NotNull LootContext context) {
        ItemStack tool = context.get(LootContextKeys.TOOL);

        return tool == null || toolCheck.verify(context, tool);
    }
}
