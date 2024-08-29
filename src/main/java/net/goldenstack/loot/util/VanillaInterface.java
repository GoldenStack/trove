package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public interface VanillaInterface {

    @Nullable Integer getScore(@NotNull Entity entity, @NotNull String objective);

    @Nullable Integer getScore(@NotNull String name, @NotNull String objective);

    @NotNull BinaryTag serializeEntity(@NotNull Entity entity);

    @NotNull ItemStack enchantItem(@NotNull Random random, @NotNull ItemStack item, int levels, @Nullable List<DynamicRegistry.Key<Enchantment>> enchantments);

    @Nullable ItemStack smelt(@NotNull ItemStack input);

}
