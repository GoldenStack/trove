package net.goldenstack.loot.util.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

// TODO: Incomplete

@SuppressWarnings("UnstableApiUsage")
public interface ItemPredicate {

    @NotNull Codec<ItemPredicate> CODEC = Codec.UNIT.transform(a -> item -> false, a -> Unit.INSTANCE);

    boolean test(@NotNull ItemStack itemStack);

}
