package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * A condition that inverts the result of the child condition.
 * @param original the condition to invert
 */
public record InvertedCondition(@NotNull LootCondition original) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:inverted";

    /**
     * A standard map-based converter for inverted conditions.
     */
    public static final @NotNull TypedLootConverter<InvertedCondition> CONVERTER =
            converter(InvertedCondition.class,
                    field(LootCondition.class).name("original").nodePath("term")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return !original.verify(context);
    }
}
