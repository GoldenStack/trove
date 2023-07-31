package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.EnchantedBookMeta;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.typeList;

/**
 * Randomly adds one enchantment, of a random valid level, to each provided item. If {@link #validEnchantments()} is
 * empty, a random enchantment that {@link Registry.EnchantmentEntry#isDiscoverable() is discoverable} and that can also
 * be applied to the provided item (via {@link dev.goldenstack.loot.minestom.VanillaInterface#canApplyEnchantment(ItemStack, Enchantment)}
 * is picked. The added enchantment has a random valid level.
 * @param conditions the conditions required for use
 * @param validEnchantments the list of possible enchantments to add
 */
public record RandomlyEnchantModifier(@NotNull List<LootCondition> conditions, @NotNull List<Enchantment> validEnchantments) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:enchant_randomly";

    /**
     * A standard map-based converter for randomly enchant modifiers.
     */
    public static final @NotNull TypedLootConverter<RandomlyEnchantModifier> CONVERTER =
            converter(RandomlyEnchantModifier.class,
                    typeList(LootCondition.class).name("conditions").withDefault(List::of),
                    typeList(Enchantment.class).name("validEnchantments").nodePath("enchantments").withDefault(List::of)
            );

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

        List<Enchantment> enchantments = this.validEnchantments;

        // Find the list of valid enchantments
        if (enchantments.isEmpty()) {
            List<Enchantment> newEnchantments = new ArrayList<>(Enchantment.values());

            newEnchantments.removeIf(ench -> !ench.registry().isDiscoverable()|| !vanilla.canApplyEnchantment(input, ench));

            if (newEnchantments.isEmpty()) {
                return input;
            }

            enchantments = newEnchantments;
        }

        var random = context.random();
        Enchantment pickedEnchantment = enchantments.get(random.nextInt(enchantments.size()));

        int level = random.nextInt((int) Math.round(pickedEnchantment.registry().maxLevel())) + 1;

        ItemStack newItem = input;
        if (newItem.material() == Material.BOOK) {
            // Store enchantments to re-add later
            var itemEnchantments = newItem.meta().getEnchantmentMap();

            // Remove enchantments
            newItem = newItem.withMeta(meta -> meta.enchantments(Map.of()));

            // Switch type
            newItem = ItemStack.builder(Material.ENCHANTED_BOOK).amount(newItem.amount()).meta(newItem.meta()).build();

            // Re-add enchantments as stored enchantments
            newItem = newItem.withMeta(EnchantedBookMeta.class, meta -> meta.enchantments(itemEnchantments));
        }

        return newItem.withMeta(meta -> meta.enchantment(pickedEnchantment, (short) level));
    }
}
