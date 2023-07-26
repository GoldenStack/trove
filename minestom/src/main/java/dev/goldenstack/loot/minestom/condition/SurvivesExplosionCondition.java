package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;

/**
 * A condition that returns true if the explosion was considered survived. This is simply a check for if a random number
 * from 0 (inclusive) to 1 (exclusive) is less than (or equal to) {@code 1 / }{@link LootContextKeys#EXPLOSION_RADIUS}
 */
public record SurvivesExplosionCondition() implements LootCondition {

    /**
     * A standard map-based converter for explosion survival conditions.
     */
    public static final @NotNull KeyedLootConverter<SurvivesExplosionCondition> CONVERTER =
            converter(SurvivesExplosionCondition.class).keyed("minecraft:survives_explosion");

    @Override
    public boolean verify(@NotNull LootContext context) {
        Float radius = context.get(LootContextKeys.EXPLOSION_RADIUS);
        return radius == null || context.random().nextFloat() <= (1 / radius);
    }
}
