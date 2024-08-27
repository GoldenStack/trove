package net.goldenstack.loot;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot table.
 * @param pools the pools that generate items in this table
 * @param functions the functions applied to each output item of this table
 */
public record LootTable(@NotNull List<LootPool> pools, @NotNull List<LootFunction> functions) implements LootGenerator {
    @Override
    public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
        List<ItemStack> items = new ArrayList<>();

        for (var pool : pools) {
            for (var item : pool.apply(context)) {
                items.add(LootFunction.apply(functions, item, context));
            }
        }

        return items;
    }
}
