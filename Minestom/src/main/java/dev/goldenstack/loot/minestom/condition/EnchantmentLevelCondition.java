package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.MinestomTypes;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.implicit;

/**
 * Verifies each context based on the chance of the level of {@link #enchantment()} on {@link LootContextKeys#TOOL} in
 * {@link #chances()}.
 */
public record EnchantmentLevelCondition(@NotNull Enchantment enchantment,
                                        @NotNull List<Double> chances) implements LootCondition {

    /**
     * A standard map-based converter for enchantment level conditions.
     */
    public static final @NotNull KeyedLootConverter<BlockStateCondition> CONVERTER =
            converter(BlockStateCondition.class,
                MinestomTypes.enchantment().name("enchantment"),
                implicit(float.class).list().name("chances")
            ).keyed("minecraft:table_bonus");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var tool = context.get(LootContextKeys.TOOL);

        int level = tool != null ? tool.meta().getEnchantmentMap().getOrDefault(this.enchantment, (short) 0) : 0;

        double chance = chances.get(Math.min(level, chances.size() - 1));

        return context.random().nextDouble() < chance;
    }
}
