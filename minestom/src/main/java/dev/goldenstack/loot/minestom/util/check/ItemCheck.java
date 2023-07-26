package dev.goldenstack.loot.minestom.util.check;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.EnchantedBookMeta;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Field.field;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

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
     * The general converter for item checks.
     */
    public static final @NotNull TypedLootConverter<ItemCheck> CONVERTER =
            converter(ItemCheck.class,
                    field(NBTCheck.class, NBTCheck.CONVERTER).name("nbtCheck").nodePath("nbt"),
                    numberRange().name("count"),
                    numberRange().name("durability"),
                    field(EnchantmentCheck.class, EnchantmentCheck.CONVERTER).list().withDefault(List::of)
                            .name("enchantmentChecks").nodePath("enchantments"),
                    field(EnchantmentCheck.class, EnchantmentCheck.CONVERTER).list().withDefault(List::of)
                            .name("storedEnchantmentChecks").nodePath("stored_enchantments"),
                    material().list().name("validMaterials").nodePath("items").optional(),
                    tag(Tag.BasicType.ITEMS).name("materialTag").nodePath("tag").optional(),
                    namespaceId().name("potionId").nodePath("potion").optional()
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
