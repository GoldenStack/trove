package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.generation.LootGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry in a loot table that can generate a list of {@link Option options} that each have their own loot and weight.
 * @param <L> the loot item type
 */
public interface LootEntry<L> {

    /**
     * Generates any number of possible options to choose from when generating loot.
     * @param context the context object, to use if required
     * @return a list, with undetermined mutability, containing the options that were generated
     */
    @NotNull List<Option<L>> requestOptions(@NotNull LootGenerationContext context);

    /**
     * An option, generated from an entry, that could potentially be chosen.
     * @param <L> the loot item type
     */
    interface Option<L> extends LootGenerator<L> {

        /**
         * Calculates the weight of this option, to be used when choosing which options should be used.
         * This number should not be below 1.<br>
         * When using the result of this method, be aware of the fact that it's valid for implementations of this method
         * to return different values even when the provided context is the identical.
         * @param context the context object, to use if required
         * @return the weight of this option
         */
        @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootGenerationContext context);

    }

    /**
     * Generates loot from the provided entries. This process is repeated {@code rolls} times. For each of the provided
     * entries, {@link #requestOptions(LootGenerationContext) LootEntry#requestOptions} is only called once and
     * {@link LootEntry.Option#getWeight(LootGenerationContext) LootEntry.Option#getWeight} is only called once, so it
     * is theoretically safe for them to return different results even if the context is the same.<br>
     * To be specific, for each roll, the entries are consolidated into options via #requestOptions, a random option
     * from them is determined via each option's weight, and that option is used to generate loot.<br>
     * This is in the core library because, although it's not the only way to generate loot, it's a pretty
     * straightforward way that will usually be the method used.
     * @param entries the entries to generate for
     * @param rolls the number of times to generate loot from the entries
     * @param context the context object, to use if required
     * @return the generated list of loot items
     * @param <L> the loot item type
     */
    static <L> @NotNull List<L> generateLoot(@NotNull List<LootEntry<L>> entries, long rolls, @NotNull LootGenerationContext context) {
        List<L> items = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            // Weight and choices must be recalculated each time as their results theoretically may change
            List<LootEntry.Option<L>> options = new ArrayList<>();
            for (LootEntry<L> entry : entries) {
                options.addAll(entry.requestOptions(context));
            }

            if (options.isEmpty()) {
                continue;
            }

            long totalWeight = 0;
            long[] lowerWeightMilestones = new long[options.size()];
            for (int j = 0; j < options.size(); j++) {
                lowerWeightMilestones[j] = totalWeight;
                // Prevent the weight of this option from being less than 1
                totalWeight += Math.max(1, options.get(j).getWeight(context));
            }

            long value = context.random().nextLong(0, totalWeight);

            LootEntry.Option<L> option = options.get(options.size() - 1);

            for (int j = 0; j < lowerWeightMilestones.length; j++) {
                if (value >= lowerWeightMilestones[j]) {
                    option = options.get(j);
                    break;
                }
            }

            items.addAll(option.generate(context));
        }
        return items;
    }

}
