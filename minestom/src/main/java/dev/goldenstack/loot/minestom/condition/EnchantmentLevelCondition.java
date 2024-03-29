package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Verifies each context based on the chance of the level of {@link #addedEnchantment()} on {@link LootContextKeys#TOOL}
 * in {@link #chances()}.
 */
public record EnchantmentLevelCondition(@NotNull Enchantment addedEnchantment,
                                        @NotNull List<Double> chances) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:table_bonus";

    /**
     * A standard map-based serializer for enchantment level conditions.
     */
    public static final @NotNull TypeSerializer<EnchantmentLevelCondition> SERIALIZER =
            serializer(EnchantmentLevelCondition.class,
                field(Enchantment.class).name("addedEnchantment").nodePath("enchantment"),
                field(Double.class).name("chances").as(list())
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var tool = context.get(LootContextKeys.TOOL);

        int level = tool != null ? tool.meta().getEnchantmentMap().getOrDefault(this.addedEnchantment, (short) 0) : 0;

        double chance = chances.get(Math.min(level, chances.size() - 1));

        return context.random().nextDouble() < chance;
    }
}
