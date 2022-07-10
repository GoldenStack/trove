package dev.goldenstack.loot.minestom.check;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.EnchantedBookMeta;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static @NotNull JsonElement serialize(@NotNull ItemCheck check, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        JsonObject object = new JsonObject();
        object.add("count", LootNumberRange.serialize(check.count(), context));
        object.add("durability", LootNumberRange.serialize(check.durability(), context));
        object.add("enchantments", JsonUtils.serializeJsonArray(check.enchantmentChecks(), eCheck -> EnchantmentCheck.serialize(eCheck, context)));
        object.add("stored_enchantments", JsonUtils.serializeJsonArray(check.storedEnchantmentChecks(), eCheck -> EnchantmentCheck.serialize(eCheck, context)));
        if (check.tagGroup() != null) {
            object.addProperty("tag", check.tagGroup().getName().asString());
        }
        if (check.validMaterials() != null) {
            object.add("items", JsonUtils.serializeJsonArray(check.validMaterials(), material -> new JsonPrimitive(material.name())));
        }
        object.add("nbt", NBTCheck.serialize(check.nbtCheck(), context));
        if (check.potionEffect() != null) {
            object.addProperty("potion", check.potionEffect().name());
        }
        return object;
    }

    public static @NotNull ItemCheck deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        JsonObject object = JsonUtils.assureJsonObject(element, null);

        LootNumberRange count = LootNumberRange.deserialize(object.get("count"), context);
        LootNumberRange durability = LootNumberRange.deserialize(object.get("durability"), context);
        List<EnchantmentCheck> enchantmentChecks = object.has("enchantments") ?
                JsonUtils.deserializeJsonArray(JsonUtils.assureJsonArray(object.get("enchantments"), "enchantments"),
                        "enchantments", (e, k) -> EnchantmentCheck.deserialize(e, context)) :
                List.of();
        List<EnchantmentCheck> storedEnchantmentChecks = object.has("stored_enchantments") ?
                JsonUtils.deserializeJsonArray(JsonUtils.assureJsonArray(object.get("stored_enchantments"), "stored_enchantments"),
                        "stored_enchantments", (e, k) -> EnchantmentCheck.deserialize(e, context)) :
                List.of();
        Tag tagGroup = object.has("tag") ?
                MinecraftServer.getTagManager().getTag(Tag.BasicType.ITEMS, JsonUtils.assureString(object.get("tag"), "tag")) :
                null;
        Set<Material> validMaterials = object.has("items") ?
                Set.copyOf(JsonUtils.deserializeJsonArray(
                        JsonUtils.assureJsonArray(object.get("items"), "items"),
                        "context",
                        (e, k) -> {
                            String str = JsonUtils.assureString(e, k);
                            var mat = Material.fromNamespaceId(str);
                            if (mat == null) {
                                throw new LootConversionException("Invalid material '" + str + "'");
                            }
                            return mat;
                        })) :
                null;
        NBTCheck check = NBTCheck.deserialize(object.get("nbt"), context);
        PotionType potionType = object.has("potion") ?
                PotionType.fromNamespaceId(JsonUtils.assureString(object.get("potion"), "potion")) :
                null;
        return new ItemCheck(count, durability, enchantmentChecks, storedEnchantmentChecks, tagGroup, validMaterials, check, potionType);
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
