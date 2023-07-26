package dev.goldenstack.loot.generation;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;

/**
 * Something that can generate loot.
 */
public interface LootGenerator {

    /**
     * Generates a (possibly empty) batch of loot items from the provided context.
     * @param context the context, to use if needed
     * @return the list of generated loot
     */
    @NotNull LootBatch generate(@NotNull LootContext context);

}