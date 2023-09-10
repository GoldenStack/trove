package dev.goldenstack.loot.generation;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * Something that can generate loot.
 */
public interface LootGenerator extends BiConsumer<@NotNull LootContext, @NotNull LootProcessor> {

    /**
     * Generates items from the provided context, giving them to the consumer.
     * @param context the context to use
     * @param processor the loot processor
     */
    @Override
    void accept(@NotNull LootContext context, @NotNull LootProcessor processor);

}