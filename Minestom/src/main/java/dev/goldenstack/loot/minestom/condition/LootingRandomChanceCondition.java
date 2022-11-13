package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that returns true based on {@link #chance()}, and scales {@link #lootingCoefficient()} based on the
 * entity's level of looting according to {@link dev.goldenstack.loot.minestom.VanillaInterface#getLooting(Entity)}.
 * @param chance the probability of this condition returning true
 * @param lootingCoefficient the scale of the looting value (is simply multiplied by it)
 */
public record LootingRandomChanceCondition(double chance, double lootingCoefficient) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for looting random chance conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, LootingRandomChanceCondition> CONVERTER = Utils.createKeyedConverter("minecraft:random_chance_with_looting", new TypeToken<>(){},
            (input, result, context) -> {
                    result.node("chance").set(input.chance());
                    result.node("looting_multiplier").set(input.lootingCoefficient());
            },
            (input, context) -> new LootingRandomChanceCondition(
                    input.node("chance").require(Double.class),
                    input.node("looting_multiplier").require(Double.class)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var killer = context.assure(LootContextKeys.KILLER_ENTITY);
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

        return context.random().nextDouble() < (chance + vanilla.getLooting(killer) * lootingCoefficient);
    }
}

