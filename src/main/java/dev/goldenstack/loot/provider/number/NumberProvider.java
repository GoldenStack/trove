package dev.goldenstack.loot.provider.number;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something that can provide a double or an integer based on the LootContext that is provided.
 */
public interface NumberProvider {

    /**
     * Generates a double based on the provided LootContext.
     */
    double getDouble(@NotNull LootContext context);

    /**
     * Generates an integer based on the provided LootContext. By default, it returns the double from
     * {@link #getDouble(LootContext)} rounded to the nearest integer.
     */
    default int getInteger(@NotNull LootContext context) {
        return (int) Math.round(getDouble(context));
    }
}
