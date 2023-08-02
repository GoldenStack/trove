package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * Verifies that the result of the provided value is within the provided range.
 * @param range the range of valid values
 * @param value the value to verify
 */
public record NumberConstraintCondition(@NotNull LootNumberRange range, @NotNull LootNumber value) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:value_check";

    /**
     * A standard map-based converter for value check conditions.
     */
    public static final @NotNull TypedLootConverter<NumberConstraintCondition> CONVERTER =
            converter(NumberConstraintCondition.class,
                    field(LootNumberRange.class).name("range"),
                    field(LootNumber.class).name("value")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return range.check(context, value.getLong(context));
    }
}

