package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that returns true based on {@link #chance()}. A chance of 0 has a 0% chance to return true, and a
 * chance of 1 has a 100% chance to return true.
 * @param chance the probability of this condition returning true
 */
public record RandomChanceCondition(double chance) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for random chance conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, RandomChanceCondition> CONVERTER = Utils.createKeyedConverter("minecraft:random_chance", new TypeToken<>(){},
            (input, result, context) ->
                    result.node("chance").set(input.chance()),
            (input, context) -> new RandomChanceCondition(
                    input.node("chance").require(Double.class)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return context.random().nextDouble() < chance;
    }
}
