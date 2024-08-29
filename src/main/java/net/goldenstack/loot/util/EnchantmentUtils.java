package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class EnchantmentUtils {

    private EnchantmentUtils() {}

    public static final BinaryTagSerializer<List<DynamicRegistry.Key<Enchantment>>> KEYS_RAW = Serial.<Enchantment>key().list();

    public static final @NotNull BinaryTagSerializer<List<DynamicRegistry.Key<Enchantment>>> TAG_LIST = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull List<DynamicRegistry.Key<Enchantment>> value) {
            return KEYS_RAW.write(context, value);
        }

        @Override
        public @NotNull List<DynamicRegistry.Key<Enchantment>> read(@NotNull Context context, @NotNull BinaryTag tag) {
            return switch (tag) {
                case StringBinaryTag string -> {
                    if (string.value().startsWith("#")) {
                        List<DynamicRegistry.Key<Enchantment>> values = new ArrayList<>();
                        MinecraftServer.getTagManager()
                                .getTag(Tag.BasicType.ENCHANTMENTS, string.value().substring(1))
                                .getValues()
                                .forEach(value -> values.add(DynamicRegistry.Key.of(value)));
                        yield values;
                    } else {
                        yield List.of(DynamicRegistry.Key.of(string.value()));
                    }
                }
                case ListBinaryTag list -> KEYS_RAW.read(context, list);
                default -> throw new IllegalArgumentException();
            };
        }
    };

    public static int level(@Nullable ItemStack item, @NotNull DynamicRegistry.Key<Enchantment> key) {
        if (item == null) return 0;

        EnchantmentList enchantments = item.get(ItemComponent.ENCHANTMENTS);
        if (enchantments == null) return 0;

        return enchantments.enchantments().getOrDefault(key, 0);
    }

    public static int level(@Nullable Entity entity, @NotNull DynamicRegistry.Key<Enchantment> key) {
        int level = 0;
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                EnchantmentList ench = living.getEquipment(slot).get(ItemComponent.ENCHANTMENTS);
                if (ench == null) continue;

                level = Math.max(level, ench.level(key));
            }
        }

        return level;
    }

    public static @NotNull ItemStack modifyItem(@NotNull ItemStack item, @NotNull Consumer<Map<DynamicRegistry.Key<Enchantment>, Integer>> enchantments) {
        DataComponent<EnchantmentList> type = item.material().equals(Material.ENCHANTED_BOOK) ? ItemComponent.STORED_ENCHANTMENTS : ItemComponent.ENCHANTMENTS;

        EnchantmentList component = item.get(type, EnchantmentList.EMPTY);

        var map = new HashMap<>(component.enchantments());
        enchantments.accept(map);

        // Make the book enchanted!
        if (!map.isEmpty() && item.material().equals(Material.BOOK)) {
            return item.builder()
                    .material(Material.ENCHANTED_BOOK)
                    .set(ItemComponent.STORED_ENCHANTMENTS, new EnchantmentList(map, component.showInTooltip()))
                    .build();
        } else {
            return item.with(type, new EnchantmentList(map, component.showInTooltip()));
        }

    }

}
