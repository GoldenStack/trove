package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Assures that the world's time is within a certain range, obeying a certain period.
 * @param range the valid range of time
 * @param period the (optional) period of each day
 */
public record TimeCheckCondition(@NotNull LootNumberRange range, @Nullable Long period) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for time check conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, TimeCheckCondition> CONVERTER = Utils.createKeyedConverter("minecraft:time_check", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("value").set(LootNumberRange.CONVERTER.serialize(input.range(), context));
                result.node("period").set(input.period());
            },
            (input, context) -> new TimeCheckCondition(
                    LootNumberRange.CONVERTER.deserialize(input.node("value"), context),
                    input.node("period").getLong()
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var time = context.assure(LootContextKeys.WORLD).getTime();
        if (period != null) {
            time %= period;
        }
        return range.check(context, time);
    }
}

