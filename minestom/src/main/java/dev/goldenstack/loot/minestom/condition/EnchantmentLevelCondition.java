package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

/**
 * Verifies each context based on the chance of the level of {@link #addedEnchantment()} on {@link LootContextKeys#TOOL}
 * in {@link #chances()}.
 */
public record EnchantmentLevelCondition(@NotNull Enchantment addedEnchantment,
                                        @NotNull List<Double> chances) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:table_bonus";

    /**
     * A standard map-based converter for enchantment level conditions.
     */
    public static final @NotNull TypedLootConverter<EnchantmentLevelCondition> CONVERTER =
            converter(EnchantmentLevelCondition.class,
                type(Enchantment.class).name("addedEnchantment").nodePath("enchantment"),
                typeList(Double.class).name("chances")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var tool = context.get(LootContextKeys.TOOL);

        int level = tool != null ? tool.meta().getEnchantmentMap().getOrDefault(this.addedEnchantment, (short) 0) : 0;

        double chance = chances.get(Math.min(level, chances.size() - 1));

        return context.random().nextDouble() < chance;
    }
}
