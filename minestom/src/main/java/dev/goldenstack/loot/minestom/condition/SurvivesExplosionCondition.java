package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A condition that returns true if the explosion was considered survived. This is simply a check for if a random number
 * from 0 (inclusive) to 1 (exclusive) is less than (or equal to) {@code 1 / }{@link LootContextKeys#EXPLOSION_RADIUS}
 */
public record SurvivesExplosionCondition() implements LootCondition {

    public static final @NotNull String KEY = "minecraft:survives_explosion";

    /**
     * A standard map-based serializer for explosion survival conditions.
     */
    public static final @NotNull TypeSerializer<SurvivesExplosionCondition> SERIALIZER =
            serializer(SurvivesExplosionCondition.class);

    @Override
    public boolean verify(@NotNull LootContext context) {
        Float radius = context.get(LootContextKeys.EXPLOSION_RADIUS);
        return radius == null || context.random().nextFloat() <= (1 / radius);
    }
}
