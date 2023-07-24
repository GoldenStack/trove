package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;

/**
 * A condition that inverts the result of the child condition.
 * @param original the condition to invert
 */
public record InvertedCondition(@NotNull LootCondition original) implements LootCondition {

    /**
     * A standard map-based converter for inverted conditions.
     */
    public static final @NotNull KeyedLootConverter<InvertedCondition> CONVERTER =
            converter(InvertedCondition.class,
                    condition().name("original").nodePath("term")
            ).keyed("minecraft:inverted");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return !original.verify(context);
    }
}
