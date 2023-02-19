package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;

/**
 * A condition that simply requires a player to have been involved in the killing of the entity somehow.
 */
public record KilledByPlayerCondition() implements LootCondition {

    /**
     * A standard map-based converter for killed-by-player conditions.
     */
    public static final @NotNull KeyedLootConverter<KilledByPlayerCondition> CONVERTER =
            converter(KilledByPlayerCondition.class).keyed("minecraft:killed_by_player");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return context.has(LootContextKeys.LAST_DAMAGE_PLAYER);
    }
}
