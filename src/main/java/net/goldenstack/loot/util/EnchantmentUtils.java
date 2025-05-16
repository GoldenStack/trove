package net.goldenstack.loot.util;

import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class EnchantmentUtils {

    private EnchantmentUtils() {
    }

    public static final @NotNull Codec<List<DynamicRegistry.Key<Enchantment>>> TAG_LIST = Codec.RegistryKey(Registries::enchantment).list()
            .orElse(Codec.STRING.transform(string -> {
                if (string.startsWith("#")) {
                    List<DynamicRegistry.Key<Enchantment>> values = new ArrayList<>();
                    MinecraftServer.getTagManager()
                            .getTag(Tag.BasicType.ENCHANTMENTS, string.substring(1))
                            .getValues()
                            .forEach(value -> values.add(DynamicRegistry.Key.of(value)));
                    return values;
                } else {
                    return List.of(DynamicRegistry.Key.of(string));
                }
            }, ignored -> {
                throw new UnsupportedOperationException();
            }));

    public static int level(@Nullable ItemStack item, @NotNull DynamicRegistry.Key<Enchantment> key) {
        if (item == null) return 0;

        EnchantmentList enchantments = item.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) return 0;

        return enchantments.enchantments().getOrDefault(key, 0);
    }

    public static int level(@Nullable Entity entity, @NotNull DynamicRegistry.Key<Enchantment> key) {
        int level = 0;
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                EnchantmentList ench = living.getEquipment(slot).get(DataComponents.ENCHANTMENTS);
                if (ench == null) continue;

                level = Math.max(level, ench.level(key));
            }
        }

        return level;
    }

    public static @NotNull ItemStack modifyItem(@NotNull ItemStack item, @NotNull Consumer<Map<DynamicRegistry.Key<Enchantment>, Integer>> enchantments) {
        DataComponent<EnchantmentList> type = item.material().equals(Material.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;

        EnchantmentList component = item.get(type, EnchantmentList.EMPTY);

        var map = new HashMap<>(component.enchantments());
        enchantments.accept(map);

        // Make the book enchanted!
        if (!map.isEmpty() && item.material().equals(Material.BOOK)) {
            return item.builder()
                    .material(Material.ENCHANTED_BOOK)
                    .set(DataComponents.STORED_ENCHANTMENTS, new EnchantmentList(map))
                    .build();
        } else {
            return item.with(type, new EnchantmentList(map));
        }

    }

}
