package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public interface VanillaInterface {

    @Nullable Integer getScore(@NotNull Entity entity, @NotNull String objective);

    @NotNull BinaryTag serializeEntity(@NotNull Entity entity);

    @NotNull ItemStack enchantItem(@NotNull Random random, @NotNull ItemStack item, int levels, @Nullable List<DynamicRegistry.Key<Enchantment>> enchantments);

    @NotNull List<ItemStack> getDynamicDrops(@NotNull NamespaceID choiceID, @NotNull CompoundBinaryTag blockNBT);

}
