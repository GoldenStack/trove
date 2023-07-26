package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.util.nbt.NBTUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

import java.util.List;
import java.util.Random;

/**
 * A vanilla interface implementation that provides sensible defaults for each method.
 */
public interface FallbackVanillaInterface extends VanillaInterface {

    @Override
    default boolean isRaining(@NotNull Instance instance) {
        return false;
    }

    @Override
    default boolean isThundering(@NotNull Instance instance) {
        return false;
    }

    @Override
    default int getLooting(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return 0;
        }

        return living.getItemInMainHand().meta().getEnchantmentMap().getOrDefault(Enchantment.LOOTING, (short) 0);
    }

    @Override
    default @NotNull ItemStack enchantItem(@NotNull Random random, @NotNull ItemStack item, int levels, boolean permitTreasure) {
        return item;
    }

    @Override
    default boolean canApplyEnchantment(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        return true;
    }

    @Override
    default @Nullable ItemStack smeltItem(@NotNull ItemStack item) {
        return item;
    }

    @Override
    default @NotNull NBTCompound getEntityNBT(@NotNull Entity entity) {
        return new NBTCompound();
    }

    @Override
    default @Nullable NBTCompound getCommandStorageValue(@NotNull NamespaceID key) {
        return null;
    }

    @Override
    @NotNull
    default List<ItemStack> getDynamicDrops(@NotNull NamespaceID dropType, @NotNull NBTCompound nbt) {
        if (NamespaceID.from("minecraft:contents").equals(dropType)) {
            NBTList<NBTCompound> contents = nbt.getList("Items");
            return (contents != null) ? NBTUtils.listToItems(contents) : List.of();
        }

        return List.of();
    }
}
