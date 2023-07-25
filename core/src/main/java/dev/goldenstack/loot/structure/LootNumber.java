package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;

/**
 * Generates numbers based on loot contexts that are provided.
 */
public interface LootNumber {

    /**
     * Generates a long depending on the information in the provided context.<br>
     * When using the result of this method, be aware of the fact that it's valid for implementations of this method to
     * return different values even when the provided context is the identical.
     * @param context the context object, to use if required
     * @return the long generated by this loot number for the provided context
     */
    long getLong(@NotNull LootGenerationContext context);

    /**
     * Generates a double depending on the information in the provided context.<br>
     * When using the result of this method, be aware of the fact that it's valid for implementations of this method to
     * return different values even when the provided context is the identical.
     * @param context the context object, to use if required
     * @return the double generated by this loot number for the provided context
     */
    double getDouble(@NotNull LootGenerationContext context);

}