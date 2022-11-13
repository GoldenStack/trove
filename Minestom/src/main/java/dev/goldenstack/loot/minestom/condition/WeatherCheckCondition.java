package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Checks that the world's weather fits the required conditions.
 * @param raining the required value of the world's rain status, or null if not required
 * @param thundering the required value of the world's thunder status, or null if not required
 */
public record WeatherCheckCondition(@Nullable Boolean raining, @Nullable Boolean thundering) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for weather check conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, WeatherCheckCondition> CONVERTER = Utils.createKeyedConverter("minecraft:weather_check", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("raining").set(input.raining);
                result.node("thundering").set(input.thundering);
            }, (input, context) -> new WeatherCheckCondition(
                    input.node("raining").get(Boolean.class),
                    input.node("thundering").get(Boolean.class)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);
        var instance = context.assure(LootContextKeys.WORLD);

        return (this.raining == null || vanilla.isRaining(instance) == this.raining) &&
                (this.thundering == null || vanilla.isThundering(instance) == this.thundering);
    }
}
