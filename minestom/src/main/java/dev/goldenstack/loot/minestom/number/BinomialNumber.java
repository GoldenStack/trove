package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.number;

/**
 * Generates numbers that follow binomial distribution.
 * @param trials the number of trials
 * @param probability the probability of a success, from 0 to 1
 */
public record BinomialNumber(@NotNull LootNumber trials, @NotNull LootNumber probability) implements LootNumber {

    /**
     * A standard map-based converter for binomial numbers.
     */
    public static final @NotNull KeyedLootConverter<BinomialNumber> CONVERTER =
            converter(BinomialNumber.class,
                    number().name("trials").nodePath("n"),
                    number().name("probability").nodePath("p")
            ).keyed("minecraft:binomial");

    @Override
    public long getLong(@NotNull LootGenerationContext context) {
        long trials = trials().getLong(context);
        double probability = probability().getDouble(context);
        int successes = 0;
        for (int trial = 0; trial < trials; trial++) {
            if (context.random().nextDouble() < probability) {
                successes++;
            }
        }
        return successes;
    }

    @Override
    public double getDouble(@NotNull LootGenerationContext context) {
        return getLong(context);
    }
}
