package dev.goldenstack.loot.minestom.check;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Map;

/**
 * When presented with a context and an enchantment map, if {@link #enchantment()} is null, returns true if all
 * enchantments pass {@link #range()}. Otherwise, returns true if just this object's enchantment in the map fits
 * {@code range()}.
 * @param enchantment the (optional) enchantment to verify
 * @param range the range to check levels
 */
public record EnchantmentCheck(@Nullable Enchantment enchantment, @NotNull LootNumberRange range) {

    public static @NotNull ConfigurationNode serialize(@NotNull EnchantmentCheck check, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        ConfigurationNode node = context.loader().createNode();
        if (check.enchantment() != null) {
            node.node("enchantment").set(check.enchantment().name());
        }
        node.node("levels").set(LootNumberRange.serialize(check.range(), context));
        return node;
    }

    public static @NotNull EnchantmentCheck deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        var range = LootNumberRange.deserialize(node.node("levels"), context);

        var enchantmentNode = node.node("enchantment");
        if (enchantmentNode.empty()) {
            return new EnchantmentCheck(null, range);
        }
        String id = enchantmentNode.getString();
        if (id == null) {
            throw new ConfigurateException(enchantmentNode, "Expected the provided node to be a string");
        }
        Enchantment enchantment = Enchantment.fromNamespaceId(id);
        if (enchantment == null) {
            throw new ConfigurateException(enchantmentNode, "Invalid enchantment '" + id + "'");
        }
        return new EnchantmentCheck(enchantment, range);
    }

    /**
     * @param context the context to feed into {@link #range()}
     * @param enchantments the enchantment map to verify
     * @return if {@link #enchantment()} is null, returns true if all enchantments pass {@link #range()}. Otherwise,
     *         returns true if just this object's enchantment fits the value in the map.
     */
    public boolean check(@NotNull LootContext context, @NotNull Map<Enchantment, Short> enchantments) {
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