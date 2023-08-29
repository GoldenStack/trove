package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * A filtered modifier that only filters items.
 */
public interface ItemStackModifier extends LootModifier.Filtered<ItemStack> {

    @Override
    default @NotNull Type filteredType() {
        return ItemStack.class;
    }

}
