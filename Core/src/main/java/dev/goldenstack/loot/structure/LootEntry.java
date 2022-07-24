package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

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
    interface Option<L> {

        /**
         * Calculates the weight of this option, to be used when choosing which options should be used.
         * This number should not be below 1.<br>
         * When using the result of this method, be aware of the fact that it's valid for implementations of this method
         * to return different values even when the provided context is the identical.
         * @param context the context object, to use if required
         * @return the weight of this option
         */
        @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootGenerationContext context);

        /**
         * Generates a list of loot based on the provided context.
         * @param context the context object, to use if required
         * @return the list of loot that was generated
         */
        @NotNull List<L> generate(@NotNull LootGenerationContext context);

    }

}
