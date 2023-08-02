package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * Assures that the world's time is within a certain range, obeying a certain period.
 * @param range the valid range of time
 * @param period the (optional) period of each day
 */
public record TimeCheckCondition(@NotNull LootNumberRange range, @Nullable Long period) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:time_check";

    /**
     * A standard map-based converter for time check conditions.
     */
    public static final @NotNull TypedLootConverter<TimeCheckCondition> CONVERTER =
            converter(TimeCheckCondition.class,
                    field(LootNumberRange.class).name("range").nodePath("value"),
                    field(Long.class).optional().name("period")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var time = context.assure(LootContextKeys.WORLD).getTime();
        if (period != null) {
            time %= period;
        }
        return range.check(context, time);
    }
}

