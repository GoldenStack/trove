package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * Generates numbers that follow binomial distribution.
 * @param trials the number of trials
 * @param probability the probability of a success, from 0 to 1
 */
public record BinomialNumber(@NotNull LootNumber trials, @NotNull LootNumber probability) implements LootNumber {

    public static final @NotNull String KEY = "minecraft:binomial";

    /**
     * A standard map-based converter for binomial numbers.
     */
    public static final @NotNull TypeSerializer<BinomialNumber> CONVERTER =
            converter(BinomialNumber.class,
                    field(LootNumber.class).name("trials").nodePath("n"),
                    field(LootNumber.class).name("probability").nodePath("p")
            );

    @Override
    public long getLong(@NotNull LootContext context) {
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
    public double getDouble(@NotNull LootContext context) {
        return getLong(context);
    }
}
