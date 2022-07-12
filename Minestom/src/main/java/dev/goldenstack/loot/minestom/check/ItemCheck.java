package dev.goldenstack.loot.minestom.check;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.minestom.util.Converters;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.util.NodeUtils;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.EnchantedBookMeta;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;
import java.util.Set;

/**
 * Assures that each field passes for any provided items.
 * @param count the range of valid {@link ItemStack#amount()} values
 * @param durability the range of valid {@link ItemMeta#getDamage()} values
 * @param enchantmentChecks the list of enchantment checks, which will each be given {@link ItemMeta#getEnchantmentMap()}
 * @param storedEnchantmentChecks the list of stored enchantment checks, which will each be given {@link EnchantedBookMeta#getStoredEnchantmentMap()}
 * @param tagGroup the (optional) tag that all items must be in to pass
 * @param validMaterials the (optional) set of materials that all items must be in to pass
 * @param nbtCheck the NBT check for any item's internal NBT
 * @param potionEffect the required potion effect of the item
 */
public record ItemCheck(@NotNull LootNumberRange count,
                        @NotNull LootNumberRange durability,
                        @NotNull List<EnchantmentCheck> enchantmentChecks,
                        @NotNull List<EnchantmentCheck> storedEnchantmentChecks,
                        @Nullable Tag tagGroup,
                        @Nullable Set<Material> validMaterials,
                        @NotNull NBTCheck nbtCheck,
                        @Nullable PotionType potionEffect) {

    public static @NotNull ConfigurationNode serialize(@NotNull ItemCheck check, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        ConfigurationNode node = context.loader().createNode();
        node.node("count").set(LootNumberRange.serialize(check.count(), context));
        node.node("durability").set(LootNumberRange.serialize(check.durability(), context));
        node.node("enchantments").set(NodeUtils.serializeList(check.enchantmentChecks(), EnchantmentCheck::serialize, context));
        node.node("stored_enchantments").set(NodeUtils.serializeList(check.storedEnchantmentChecks(), EnchantmentCheck::serialize, context));
        node.node("tag").set(Converters.ITEM_TAG_CONVERTER.serializeNullable(check.tagGroup(), context));
        node.node("items").set(NodeUtils.serializeList(check.validMaterials(), Converters.MATERIAL_CONVERTER::serialize, context));
        node.node("nbt").set(NBTCheck.serialize(check.nbtCheck(), context));
        node.node("potion").set(Converters.POTION_TYPE_CONVERTER.serializeNullable(check.potionEffect(), context));
        return node;
    }

    public static @NotNull ItemCheck deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        return new ItemCheck(
                LootNumberRange.deserialize(node.node("count"), context),
                LootNumberRange.deserialize(node.node("durability"), context),
                NodeUtils.deserializeList(node.node("enchantments"), EnchantmentCheck::deserialize, context),
                NodeUtils.deserializeList(node.node("stored_enchantments"), EnchantmentCheck::deserialize, context),
                Converters.ITEM_TAG_CONVERTER.deserialize(node.node("tag"), context),
                Set.copyOf(NodeUtils.deserializeList(node.node("items"), Converters.MATERIAL_CONVERTER::deserialize, context)),
                NBTCheck.deserialize(node.node("nbt"), context),
                Converters.POTION_TYPE_CONVERTER.deserializeNullable(node.node("potion"), context)
        );
    }

    public ItemCheck {
        enchantmentChecks = List.copyOf(enchantmentChecks);
        storedEnchantmentChecks = List.copyOf(storedEnchantmentChecks);
        if (validMaterials != null) {
            validMaterials = Set.copyOf(validMaterials);
        }
    }

    /**
     * Note: more information on how this method works can be found in the documentation of the class itself.
     * @param context the context to use for checking
     * @param itemStack the item to check
     * @return assures that each of this check's parts accept the provided item
     */
    @SuppressWarnings("UnstableApiUsage")
    public boolean check(@NotNull LootContext context, @NotNull ItemStack itemStack) {
        if (!count.check(context, itemStack.amount())) {
            return false;
        }

        int maxDamage = itemStack.material().registry().maxDamage();
        if (maxDamage != 0 && !durability.check(context, maxDamage - itemStack.meta().getDamage())) {
            return false;
        }

        if (tagGroup != null && !tagGroup.contains(itemStack.material().namespace())) {
            return false;
        }

        if (validMaterials != null && !validMaterials.contains(itemStack.material())) {
            return false;
        }

        if (!nbtCheck.test(itemStack.meta().toNBT())) {
            return false;
        }

        if (potionEffect != null && potionEffect != itemStack.meta(PotionMeta.class).getPotionType()) {
            return false;
        }

        if (!enchantmentChecks.isEmpty()) {
            var map = itemStack.meta().getEnchantmentMap();
            for (var check : enchantmentChecks) {
                if (!check.check(context, map)) {
                    return false;
                }
            }
        }

        if (!storedEnchantmentChecks.isEmpty()) {
            var map = itemStack.meta(EnchantedBookMeta.class).getStoredEnchantmentMap();
            for (var check : storedEnchantmentChecks) {
                if (!check.check(context, map)) {
                    return false;
                }
            }
        }

        return true;
    }
}
