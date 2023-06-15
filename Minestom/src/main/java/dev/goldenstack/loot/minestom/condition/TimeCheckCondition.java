package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.implicit;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.numberRange;

/**
 * Assures that the world's time is within a certain range, obeying a certain period.
 * @param range the valid range of time
 * @param period the (optional) period of each day
 */
public record TimeCheckCondition(@NotNull LootNumberRange range, @Nullable Long period) implements LootCondition {

    /**
     * A standard map-based converter for time check conditions.
     */
    public static final @NotNull KeyedLootConverter<TimeCheckCondition> CONVERTER =
            converter(TimeCheckCondition.class,
                    numberRange().name("range").nodePath("value"),
                    implicit(Long.class).optional().name("period")
            ).keyed("minecraft:time_check");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var time = context.assure(LootContextKeys.WORLD).getTime();
        if (period != null) {
            time %= period;
        }
        return range.check(context, time);
    }
}

