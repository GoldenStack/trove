package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.vanilla.VanillaInterface;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A condition that returns true based on {@link #chance()}, and scales {@link #lootingCoefficient()} based on the
 * entity's level of looting according to {@link VanillaInterface#getLooting(Entity)}.
 * @param chance the probability of this condition returning true
 * @param lootingCoefficient the scale of the looting value (is simply multiplied by it)
 */
public record LootingRandomChanceCondition(double chance, double lootingCoefficient) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:random_chance_with_looting";

    /**
     * A standard map-based serializer for looting random chance conditions.
     */
    public static final @NotNull TypeSerializer<LootingRandomChanceCondition> SERIALIZER =
            serializer(LootingRandomChanceCondition.class,
                    field(double.class).name("chance"),
                    field(double.class).name("lootingCoefficient").nodePath("looting_multiplier")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var killer = context.assure(LootContextKeys.KILLER_ENTITY);
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

        return context.random().nextDouble() < (chance + vanilla.getLooting(killer) * lootingCoefficient);
    }
}

