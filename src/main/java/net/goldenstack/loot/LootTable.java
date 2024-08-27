package net.goldenstack.loot;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LootTable() implements LootGenerator {
    @Override
    public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
        return List.of();
    }
}
