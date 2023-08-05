package dev.goldenstack.loot.minestom.util.check;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import net.minestom.server.item.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.Map;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * A check that verifies a map of an enchantment with an optional enchantment and a range. See
 * {@link #verify(LootContext, Map)} for details.
 * @param enchantmentType the optional enchantment
 * @param levels the range of valid levels
 */
public record EnchantmentCheck(@Nullable Enchantment enchantmentType, @NotNull LootNumberRange levels) {

    /**
     * A standard map-based serializer for enchantment checks.
     */
    public static final @NotNull TypeSerializer<EnchantmentCheck> CONVERTER =
            converter(EnchantmentCheck.class,
                    field(Enchantment.class).name("enchantmentType").nodePath("enchantment").optional(),
                    field(LootNumberRange.class).name("levels").optional()
            );

    /**
     * Checks to see if the provided enchantments are valid according to this check. If {@link #enchantmentType()} is
     * null, at least one of the enchantments in the map must fit the {@link #levels()}. Otherwise, only the enchantment
     * must. If the enchantment is defined but it is not present in this map, it will be considered as failing, unless
     * if the range has no minimum or maximum.
     * @param context the context that will be fed to {@link #levels()}
     * @param enchantments the enchantment map to test
     * @return true if the provided enchantments pass this check, and false otherwise
     */
    public boolean verify(@NotNull LootContext context, @NotNull Map<Enchantment, Short> enchantments) {
        if (enchantmentType != null) {
            Short level = enchantments.get(enchantmentType);
            return level != null && levels.check(context, level);
        }
        for (var entry : enchantments.entrySet()) {
            if (levels.check(context, entry.getValue())) {
                return true;
            }
        }
        // If this is true, then the list was empty and so we should always return true anyway.
        return levels().min() == null && levels.max() == null;
    }

}
