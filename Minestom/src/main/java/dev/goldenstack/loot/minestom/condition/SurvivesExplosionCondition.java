package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that returns true if the explosion was considered survived. This is simply a check for if a random number
 * from 0 (inclusive) to 1 (exclusive) is less than (or equal to) {@code 1 / }{@link LootContextKeys#EXPLOSION_RADIUS}
 */
public record SurvivesExplosionCondition() implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for explosion survival conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, SurvivesExplosionCondition> CONVERTER = Utils.createKeyedConverter("minecraft:survives_explosion", new TypeToken<>(){},
            (input, result, context) -> {},
            (input, context) -> new SurvivesExplosionCondition());

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        Float radius = context.get(LootContextKeys.EXPLOSION_RADIUS);
        return radius == null || context.random().nextFloat() <= (1 / radius);
    }
}
