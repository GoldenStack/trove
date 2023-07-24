package dev.goldenstack.loot.util;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Utils {
    private Utils() {}

    /**
     * Generates loot from the provided entries. This process is repeated {@code rolls} times. For each of the provided
     * entries, {@link LootEntry#requestChoices(LootGenerationContext)} is only called once and
     * {@link LootEntry.Choice#getWeight(LootGenerationContext)} is only called once, so it is theoretically safe for
     * them to return different results even if the context is the same.<br>
     * To be specific, for each roll, the entries are consolidated into options via #requestOptions, a random choice
     * from them is determined via each choice's weight, and that choice is used to generate loot.<br>
     * This is in the core library because, although it's not the only way to generate loot, it's a pretty
     * straightforward way and will usually be the method used.
     * @param entries the entries to generate for
     * @param rolls the number of times to generate loot from the entries
     * @param context the context object, to use if required
     * @return the generated list of loot items
     */
    public static @NotNull LootBatch generateStandardLoot(@NotNull List<LootEntry> entries, long rolls, @NotNull LootGenerationContext context) {
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            // Weight and choices must be recalculated each time as their results theoretically may change
            List<LootEntry.Choice> choices = new ArrayList<>();
            for (LootEntry entry : entries) {
                choices.addAll(entry.requestChoices(context));
            }

            if (choices.isEmpty()) {
                continue;
            }

            long totalWeight = 0;
            long[] weightMilestones = new long[choices.size()];
            for (int j = 0; j < choices.size(); j++) {
                // Prevent the weight of this choice from being less than 1
                totalWeight += Math.max(1, choices.get(j).getWeight(context));

                weightMilestones[j] = totalWeight;
            }

            long value = context.random().nextLong(0, totalWeight);

            LootEntry.Choice choice = choices.get(choices.size() - 1);

            for (int j = 0; j < weightMilestones.length; j++) {
                if (value < weightMilestones[j]) {
                    choice = choices.get(j);
                    break;
                }
            }

            items.addAll(choice.generate(context).items());
        }
        return new LootBatch(items);
    }

    /**
     * Creates a converter proxied by the converter returned by {@code converterFinder}.
     * @param converterFinder the function that gets the converter
     * @return a new converter that uses the finder to determine which one it is proxying
     * @param <V> the converted type
     */
    public static <V> @NotNull LootConverter<V> converterFromContext(@NotNull Function<LootConversionContext, LootConverter<V>> converterFinder) {
        return LootConverter.join(
                (input, result, context) -> converterFinder.apply(context).serialize(input, result, context),
                (input, context) -> converterFinder.apply(context).deserialize(input, context)
        );
    }

}
