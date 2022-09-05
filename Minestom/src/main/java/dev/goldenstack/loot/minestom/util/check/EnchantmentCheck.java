package dev.goldenstack.loot.minestom.util.check;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.util.Utils;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.Map;

/**
 * A check that verifies a map of an enchantment with an optional enchantment and a range. See {@link #verify(LootGenerationContext, Map)}
 * for details.
 * @param enchantment the optional enchantment
 * @param range the range of valid values
 */
public record EnchantmentCheck(@Nullable Enchantment enchantment, @NotNull LootNumberRange range) {

    /**
     * A standard map-based serializer for enchantment checks.
     */
    public static final @NotNull LootConverter<ItemStack, EnchantmentCheck> CONVERTER = Utils.createConverter(
            (input, context) -> {
                var node = context.loader().createNode();
                if (input.enchantment != null) {
                    node.node("enchantment").set(input.enchantment.namespace().asString());
                }
                node.node("levels").set(LootNumberRange.CONVERTER.serialize(input.range, context));
                return node;
            }, (input, context) -> {
                var enchantmentNode = input.node("enchantment");
                String rawEnchantment = enchantmentNode.getString();
                LootNumberRange range = LootNumberRange.CONVERTER.deserialize(input.node("levels"), context);

                if (rawEnchantment == null) {
                    return new EnchantmentCheck(null, range);
                }
                Enchantment enchantment = Enchantment.fromNamespaceId(rawEnchantment);
                if (enchantment == null) {
                    throw new ConfigurateException(enchantmentNode, "Expected the provided node to have a valid enchantment, but found '" + rawEnchantment + "' instead.");
                }
                return new EnchantmentCheck(enchantment, range);
            }
    );

    /**
     * Checks to see if the provided enchantments are valid according to this check. If {@link #enchantment()} is null,
     * all of the enchantments in the map must fit the {@link #range()}. Otherwise, only the enchantment must. If the
     * enchantment is defined but it is not present in this map, it will be considered as failing (even if the range has
     * no minimum or maximum).
     * @param context the context that will be fed to {@link #range()}
     * @param enchantments the enchantment map to test
     * @return true if the provided enchantments pass this check, and false otherwise
     */
    public boolean verify(@NotNull LootGenerationContext context, @NotNull Map<Enchantment, Short> enchantments) {
        if (enchantment != null) {
            Short level = enchantments.get(enchantment);
            return level != null && range.check(context, level);
        }
        for (var entry : enchantments.entrySet()) {
            if (!range.check(context, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

}
