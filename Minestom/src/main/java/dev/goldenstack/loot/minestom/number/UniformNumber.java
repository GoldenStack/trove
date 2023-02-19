package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.number;

/**
 * Generates a random number between (inclusive) the minimum and maximum. To be precise, for
 * {@link #getLong(LootGenerationContext)}, both the minimum and maximum are inclusive, but for
 * {@link #getDouble(LootGenerationContext)}, only the minimum is inclusive.<br>
 * The minimum should always be less than the maximum.
 * @param min the minimum value
 * @param max the maximum value
 */
public record UniformNumber(@NotNull LootNumber min, @NotNull LootNumber max) implements LootNumber {

    /**
     * A standard map-based converter for uniform numbers.
     */
    public static final @NotNull KeyedLootConverter<UniformNumber> CONVERTER =
            converter(UniformNumber.class,
                    number().name("min"),
                    number().name("max")
            ).keyed("minecraft:uniform");

    @Override
    public long getLong(@NotNull LootGenerationContext context) {
        return context.random().nextLong(min().getLong(context), max().getLong(context) + 1);
    }

    @Override
    public double getDouble(@NotNull LootGenerationContext context) {
        return context.random().nextDouble(min().getDouble(context), max().getDouble(context));
    }
}
