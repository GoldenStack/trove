package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootAware;
import org.jetbrains.annotations.NotNull;

/**
 * Generates numbers based on the provided loot context
 * @param <L> the loot item
 */
public interface LootNumber<L> extends LootAware<L> {

    /**
     * From the provided loot context, generates a long.
     */
    long getLong(@NotNull LootContext context);

    /**
     * From the provided loot context, generates a double.
     */
    double getDouble(@NotNull LootContext context);

}
