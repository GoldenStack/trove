package net.goldenstack.loot.util.predicate;

import net.goldenstack.loot.util.Template;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public interface ItemPredicate {

    @SuppressWarnings("UnstableApiUsage")
    @NotNull AtomicReference<BinaryTagSerializer<ItemPredicate>> SERIALIZER = new AtomicReference<>(Template.template(() -> item -> false));

    boolean test(@NotNull ItemStack itemStack);

}
