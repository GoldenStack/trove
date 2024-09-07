package net.goldenstack.loot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Something that can generate loot.
 */
public interface LootGenerator {

    @NotNull List<ItemStack> generate(@NotNull LootContext context);

    default void blockDrop(@NotNull LootContext context, @NotNull Instance instance, @NotNull Point block) {
        for (ItemStack item : generate(context)) {
            Trove.blockDrop(instance, item, block);
        }
    }

    default void drop(@NotNull LootContext context, @NotNull Instance instance, @NotNull Point pos) {
        for (ItemStack item : generate(context)) {
            Trove.drop(instance, item, pos);
        }
    }

}
