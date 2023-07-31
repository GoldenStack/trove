package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.type;

/**
 * A condition that returns true based on {@link #chance()}, and scales {@link #lootingCoefficient()} based on the
 * entity's level of looting according to {@link dev.goldenstack.loot.minestom.VanillaInterface#getLooting(Entity)}.
 * @param chance the probability of this condition returning true
 * @param lootingCoefficient the scale of the looting value (is simply multiplied by it)
 */
public record LootingRandomChanceCondition(double chance, double lootingCoefficient) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:random_chance_with_looting";

    /**
     * A standard map-based converter for looting random chance conditions.
     */
    public static final @NotNull TypedLootConverter<LootingRandomChanceCondition> CONVERTER =
            converter(LootingRandomChanceCondition.class,
                    type(double.class).name("chance"),
                    type(double.class).name("lootingCoefficient").nodePath("looting_multiplier")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var killer = context.assure(LootContextKeys.KILLER_ENTITY);
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

        return context.random().nextDouble() < (chance + vanilla.getLooting(killer) * lootingCoefficient);
    }
}

