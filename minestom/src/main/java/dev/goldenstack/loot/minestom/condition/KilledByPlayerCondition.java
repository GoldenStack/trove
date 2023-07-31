package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;

/**
 * A condition that simply requires a player to have been involved in the killing of the entity somehow.
 */
public record KilledByPlayerCondition() implements LootCondition {

    public static final @NotNull String KEY = "minecraft:killed_by_player";

    /**
     * A standard map-based converter for killed-by-player conditions.
     */
    public static final @NotNull TypedLootConverter<KilledByPlayerCondition> CONVERTER =
            converter(KilledByPlayerCondition.class);

    @Override
    public boolean verify(@NotNull LootContext context) {
        return context.has(LootContextKeys.LAST_DAMAGE_PLAYER);
    }
}
