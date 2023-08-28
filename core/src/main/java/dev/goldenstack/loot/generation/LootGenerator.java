package dev.goldenstack.loot.generation;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Something that can generate loot.
 */
public interface LootGenerator {

    /**
     * Generates a (possibly empty) batch of loot items from the provided context.
     * @param context the context, to use if needed
     */
    @NotNull List<Object> generate(@NotNull LootContext context);

}