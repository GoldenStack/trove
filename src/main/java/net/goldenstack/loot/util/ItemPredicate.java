package net.goldenstack.loot.util;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemPredicate {

    boolean test(@NotNull ItemStack itemStack);

}
