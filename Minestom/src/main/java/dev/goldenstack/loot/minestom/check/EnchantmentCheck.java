package dev.goldenstack.loot.minestom.check;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * When presented with a context and an enchantment map, if {@link #enchantment()} is null, returns true if all
 * enchantments pass {@link #range()}. Otherwise, returns true if just this object's enchantment in the map fits
 * {@code range()}.
 * @param enchantment the (optional) enchantment to verify
 * @param range the range to check levels
 */
public record EnchantmentCheck(@Nullable Enchantment enchantment, @NotNull LootNumberRange range) {

    public static @NotNull JsonElement serialize(@NotNull EnchantmentCheck check, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        JsonObject object = new JsonObject();
        if (check.enchantment() != null) {
            object.addProperty("enchantment", check.enchantment().name());
        }
        object.add("levels", LootNumberRange.serialize(check.range(), context));
        return object;
    }

    public static @NotNull EnchantmentCheck deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        JsonObject object = JsonUtils.assureJsonObject(element, null);
        LootNumberRange range = object.has("levels") ?
                LootNumberRange.deserialize(object.get("levels"), context) :
                new LootNumberRange(null, null);

        JsonElement rawEnchantment = object.get("enchantment");
        if (JsonUtils.isNull(rawEnchantment)) {
            return new EnchantmentCheck(null, range);
        }

        String enchantmentId = JsonUtils.assureString(rawEnchantment, "enchantment");
        Enchantment enchantment = Enchantment.fromNamespaceId(enchantmentId);
        if (enchantment == null) {
            throw new LootConversionException("Invalid enchantment '" + enchantmentId + "'");
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