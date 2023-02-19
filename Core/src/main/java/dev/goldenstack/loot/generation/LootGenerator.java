package dev.goldenstack.loot.generation;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;

/**
 * Something that can generate loot.
 */
public interface LootGenerator {

    /**
     * Generates a (possibly empty) list of loot items from the provided context. Its mutability is unspecified.
     * @param context the context, to use if needed
     * @return the list of generated loot
     */
    @NotNull LootBatch generate(@NotNull LootGenerationContext context);

}