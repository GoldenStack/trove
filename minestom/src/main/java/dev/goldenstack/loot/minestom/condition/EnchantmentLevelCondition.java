package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.enchantment;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.implicit;

/**
 * Verifies each context based on the chance of the level of {@link #addedEnchantment()} on {@link LootContextKeys#TOOL}
 * in {@link #chances()}.
 */
public record EnchantmentLevelCondition(@NotNull Enchantment addedEnchantment,
                                        @NotNull List<Double> chances) implements LootCondition {

    /**
     * A standard map-based converter for enchantment level conditions.
     */
    public static final @NotNull KeyedLootConverter<EnchantmentLevelCondition> CONVERTER =
            converter(EnchantmentLevelCondition.class,
                enchantment().name("addedEnchantment").nodePath("enchantment"),
                implicit(Double.class).list().name("chances")
            ).keyed("minecraft:table_bonus");

    @Override
    public boolean verify(@NotNull LootContext context) {
        var tool = context.get(LootContextKeys.TOOL);

        int level = tool != null ? tool.meta().getEnchantmentMap().getOrDefault(this.addedEnchantment, (short) 0) : 0;

        double chance = chances.get(Math.min(level, chances.size() - 1));

        return context.random().nextDouble() < chance;
    }
}
