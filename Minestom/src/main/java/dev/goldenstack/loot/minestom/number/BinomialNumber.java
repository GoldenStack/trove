package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Generates numbers that follow binomial distribution.
 * @param n the number of trials
 * @param p the probability of a success, from 0 to 1
 */
public record BinomialNumber(@NotNull LootNumber<ItemStack> n, @NotNull LootNumber<ItemStack> p) implements LootNumber<ItemStack> {

    /**
     * A standard map-based converter for binomial numbers.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, BinomialNumber> CONVERTER = Utils.createKeyedConverter("minecraft:binomial", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("n").set(context.loader().lootNumberManager().serialize(input.n(), context));
                result.node("p").set(context.loader().lootNumberManager().serialize(input.n(), context));
            }, (input, context) -> new BinomialNumber(
                    context.loader().lootNumberManager().deserialize(input.node("n"), context),
                    context.loader().lootNumberManager().deserialize(input.node("p"), context)
            ));

    @Override
    public long getLong(@NotNull LootGenerationContext context) {
        long trials = p().getLong(context);
        double probability = p().getDouble(context);
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
