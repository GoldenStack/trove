package net.goldenstack.loot.util;

import net.goldenstack.loot.LootFunction;
import net.goldenstack.loot.LootPredicate;
import net.goldenstack.loot.LootTable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public interface VanillaInterface {

    static @NotNull VanillaInterface defaults() {
        return new VanillaInterface() {
            @Override
            public @Nullable Integer score(@NotNull Entity entity, @NotNull String objective) {
                return null;
            }

            @Override
            public @Nullable Integer score(@NotNull String name, @NotNull String objective) {
                return null;
            }

            @Override
            public @NotNull BinaryTag serializeEntity(@NotNull Entity entity) {
                return CompoundBinaryTag.empty();
            }

            @Override
            public @NotNull ItemStack enchant(@NotNull Random random, @NotNull ItemStack item, int levels, @Nullable List<DynamicRegistry.Key<Enchantment>> enchantments) {
                return item;
            }

            @Override
            public @Nullable ItemStack smelt(@NotNull ItemStack input) {
                return null;
            }

            @Override
            public @Nullable LootTable tableRegistry(@NotNull Key key) {
                return null;
            }

            @Override
            public @Nullable LootPredicate predicateRegistry(@NotNull Key key) {
                return null;
            }

            @Override
            public @Nullable LootFunction functionRegistry(@NotNull Key key) {
                return null;
            }

            @Override
            public @Nullable CompoundBinaryTag commandStorage(@NotNull Key key) {
                return null;
            }
        };
    }

    @NotNull Tag<Component> CUSTOM_NAME = Tag.Component("CustomName");

    @NotNull Tag<List<Material>> DECORATED_POT_SHERDS = Tag.String("sherds")
            .map(Key::key, Key::asString)
            .map(Material::fromKey, Material::key)
            .list().defaultValue(List::of);

    @NotNull Tag<List<ItemStack>> CONTAINER_ITEMS = Tag.ItemStack("Items").list().defaultValue(List::of);

    @Nullable Integer score(@NotNull Entity entity, @NotNull String objective);

    @Nullable Integer score(@NotNull String name, @NotNull String objective);

    @NotNull BinaryTag serializeEntity(@NotNull Entity entity);

    @NotNull ItemStack enchant(@NotNull Random random, @NotNull ItemStack item, int levels, @Nullable List<DynamicRegistry.Key<Enchantment>> enchantments);

    @Nullable ItemStack smelt(@NotNull ItemStack input);

    @Nullable LootTable tableRegistry(@NotNull Key key);

    @Nullable LootPredicate predicateRegistry(@NotNull Key key);

    @Nullable LootFunction functionRegistry(@NotNull Key key);

    @Nullable CompoundBinaryTag commandStorage(@NotNull Key key);

}
