package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Consistently returns the same value that it was provided.
 * @param value the value to always return
 */
public record ConstantNumber(double value) implements LootNumber<ItemStack> {

    /**
     * @param context the LootContext to use to generate the number
     * @return {@code Math.round(value())}
     */
    @Override
    public long getLong(@NotNull LootContext context) {
        return Math.round(value);
    }

    /**
     * @param context the LootContext to use to generate the number
     * @return {@link #value()}
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        return value;
    }

}
