package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.conversion.LootAware;
import org.jetbrains.annotations.NotNull;

/**
 * Generates numbers based on the provided loot context
 * @param <L> the loot item
 */
public interface LootNumber<L> extends LootAware<L> {

    /**
     * From the provided loot context, generates a long.<br>
     * Note: when using results from this method, do not assume that the result will be the same even if the provided
     * context is identical. Most implementations of this will generally have the same result for the same inputs, but
     * this library does not make any guarantees.
     * @param context the LootContext to use to generate the number
     * @return the long that was generated from the provided context
     */
    long getLong(@NotNull LootContext context);

    /**
     * From the provided loot context, generates a double.<br>
     * Note: when using results from this method, do not assume that the result will be the same even if the provided
     * context is identical. Most implementations of this will generally have the same result for the same inputs, but
     * this library does not make any guarantees.
     * @param context the LootContext to use to generate the number
     * @return the double that was generated from the provided context
     */
    double getDouble(@NotNull LootContext context);

}
