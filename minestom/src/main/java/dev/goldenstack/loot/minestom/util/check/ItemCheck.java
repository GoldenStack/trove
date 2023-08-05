package dev.goldenstack.loot.minestom.util.check;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.EnchantedBookMeta;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.minestom.util.MinestomTypes.tag;
import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Represents a check for items, returning true or false depending on whether or not they were verified.
 * @param nbtCheck the NBT predicate that each item must pass
 * @param count the number range that each item's amount must be within
 * @param durability the number range that each item's remaining durability must be within
 * @param enchantmentChecks the list of enchantment checks that each item must pass, or empty to ignore
 * @param storedEnchantmentChecks the list of stored enchantment checks that each item must pass, or empty to ignore
 * @param validMaterials the list of valid materials that each provided item must fit within, or null to ignore
 * @param materialTag the item tag that each provided item must fit within, or null to ignore
 * @param potionId the namespace ID that provided potions must be (minecraft:empty for none), or null to ignore
 */
public record ItemCheck(@NotNull NBTCheck nbtCheck,
                        @NotNull LootNumberRange count, @NotNull LootNumberRange durability,
                        @NotNull List<EnchantmentCheck> enchantmentChecks, @NotNull List<EnchantmentCheck> storedEnchantmentChecks,
                        @Nullable List<Material> validMaterials, @Nullable Tag materialTag,
                        @Nullable NamespaceID potionId) {

    /**
     * An empty item check. This can be used for defaults.
     */
    public static final @NotNull ItemCheck EMPTY = new ItemCheck(
            new NBTCheck(null),
            new LootNumberRange(null, null), new LootNumberRange(null, null),
            List.of(), List.of(),
            null, null, null
    );

    /**
     * The general serializer for item checks.
     */
    public static final @NotNull TypeSerializer<ItemCheck> SERIALIZER =
            serializer(ItemCheck.class,
                    field(NBTCheck.class).fallback(new NBTCheck(null)).name("nbtCheck").nodePath("nbt"),
                    field(LootNumberRange.class).fallback(new LootNumberRange(null, null)).name("count"),
                    field(LootNumberRange.class).fallback(new LootNumberRange(null, null)).name("durability"),
                    field(EnchantmentCheck.class).name("enchantmentChecks").nodePath("enchantments").as(list()).fallback(List::of),
                    field(EnchantmentCheck.class).name("storedEnchantmentChecks").nodePath("stored_enchantments").as(list()).fallback(List::of),
                    field(Material.class).name("validMaterials").nodePath("items").as(list()).optional(),
                    field(Tag.class).name("materialTag").nodePath("tag").as(tag(Tag.BasicType.ITEMS)).optional(),
                    field(NamespaceID.class).name("potionId").nodePath("potion").optional()
            );

    @SuppressWarnings("UnstableApiUsage")
    public boolean verify(@NotNull LootContext context, @NotNull ItemStack item) {
        if (!nbtCheck.verify(item.meta().toNBT())) return false;

        if (!count.check(context, item.amount())) return false;
        if ((durability.min() != null || durability.max() != null) && item.material().registry().maxDamage() == 0) return false;
        if (!durability.check(context, item.material().registry().maxDamage() - item.meta().getDamage())) return false;

        if (materialTag != null && !materialTag.contains(item.material().namespace())) return false;
        if (validMaterials != null && !validMaterials.contains(item.material())) return false;

        // Test enchantments
        var enchantments = item.meta().getEnchantmentMap();
        for (var check : enchantmentChecks) {
            if (!check.verify(context, enchantments)) {
                return false;
            }
        }

        // Test stored enchantments
        var storedEnchantments = item.meta(EnchantedBookMeta.class).getStoredEnchantmentMap();
        for (var check : storedEnchantmentChecks) {
            if (!check.verify(context, storedEnchantments)) {
                return false;
            }
        }

        // Test potions
        var potion = item.meta().toNBT().getString("Potion");
        if (potion == null) {
            potion = "minecraft:empty";
        }
        var potionKey = NamespaceID.from(potion);
        return this.potionId == null || this.potionId == potionKey;
    }

}
