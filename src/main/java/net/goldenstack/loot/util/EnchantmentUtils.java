package net.goldenstack.loot.util;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantmentUtils {

    private EnchantmentUtils() {}

    public static int level(@Nullable ItemStack item, @NotNull NamespaceID key) {
        if (item == null) return 0;

        EnchantmentList enchantments = item.get(ItemComponent.ENCHANTMENTS);
        if (enchantments == null) return 0;

        return enchantments.enchantments().getOrDefault(DynamicRegistry.Key.of(key), 0);
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

}
