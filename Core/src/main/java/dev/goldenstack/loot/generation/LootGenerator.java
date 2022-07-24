package dev.goldenstack.loot.generation;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Something that can generate loot.
 * @param <L> the loot item type
 */
public interface LootGenerator<L> {

    /**
     * Generates a (possibly empty) list of loot items from the provided context. Its mutability is unspecified.
     * @param context the context, to use if needed
     * @return the list of generated loot
     */
    @NotNull List<L> generate(@NotNull LootGenerationContext context);

}