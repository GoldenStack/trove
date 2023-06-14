package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.itemPredicate;

/**
 * Verifies that all provided contexts have a tool that passes the {@link #toolPredicate()}.
 */
public record ToolPredicateCondition(@NotNull VanillaInterface.ItemPredicate toolPredicate) implements LootCondition {

    /**
     * A standard map-based converter for tool predicate conditions.
     */
    public static final @NotNull KeyedLootConverter<ToolPredicateCondition> CONVERTER =
            converter(ToolPredicateCondition.class,
                    itemPredicate().name("toolPredicate").nodeName("predicate")
            ).keyed("minecraft:match_tool");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        ItemStack tool = context.get(LootContextKeys.TOOL);

        return tool == null || toolPredicate.test(tool);
    }
}
