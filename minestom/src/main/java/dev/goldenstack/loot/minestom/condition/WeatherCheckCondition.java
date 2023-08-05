package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * Checks that the world's weather fits the required conditions.
 * @param raining the (optional) required value of the world's rain status, or null if not required
 * @param thundering the (optional) required value of the world's thunder status, or null if not required
 */
public record WeatherCheckCondition(@Nullable Boolean raining, @Nullable Boolean thundering) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:weather_check";

    /**
     * A standard map-based converter for weather check conditions.
     */
    public static final @NotNull TypeSerializer<WeatherCheckCondition> CONVERTER =
            converter(WeatherCheckCondition.class,
                    field(Boolean.class).optional().name("raining"),
                    field(Boolean.class).optional().name("thundering")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);
        var instance = context.assure(LootContextKeys.WORLD);

        return (this.raining == null || vanilla.isRaining(instance) == this.raining) &&
                (this.thundering == null || vanilla.isThundering(instance) == this.thundering);
    }
}
