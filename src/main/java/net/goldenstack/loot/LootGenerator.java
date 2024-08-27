package net.goldenstack.loot;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Something that can generate loot.
 */
public interface LootGenerator extends Function<@NotNull LootContext, @NotNull List<ItemStack>> {

    @Override
    @NotNull List<ItemStack> apply(@NotNull LootContext context);

}
