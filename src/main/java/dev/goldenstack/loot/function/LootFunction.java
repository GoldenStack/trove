package dev.goldenstack.loot.function;

import dev.goldenstack.loot.context.LootContext;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Represents something that can make modifications to an ItemStack based on the provided LootContext.
 */
public interface LootFunction extends BiFunction<ItemStack, LootContext, ItemStack> {

    /**
     * Makes modifications to the provided ItemStack if needed and returns the result.<br>
     * The only reason this is being overridden is so that people have to use {@link NotNull @NotNull}
     */
    @Override
    @NotNull ItemStack apply(@NotNull ItemStack itemStack, @NotNull LootContext context);
}