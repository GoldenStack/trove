package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.implicit;

/**
 * A condition that returns true based on {@link #chance()}. A chance of 0 has a 0% chance to return true, and a
 * chance of 1 has a 100% chance to return true.
 * @param chance the probability of this condition returning true
 */
public record RandomChanceCondition(double chance) implements LootCondition {

    /**
     * A standard map-based converter for random chance conditions.
     */
    public static final @NotNull KeyedLootConverter<RandomChanceCondition> CONVERTER =
            converter(RandomChanceCondition.class,
                    implicit(double.class).name("chance")
            ).keyed("minecraft:random_chance");

    @Override
    public boolean verify(@NotNull LootContext context) {
        return context.random().nextDouble() < chance;
    }
}
