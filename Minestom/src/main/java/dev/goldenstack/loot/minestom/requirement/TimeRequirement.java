package dev.goldenstack.loot.minestom.requirement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.conversion.LootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootRequirement;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Assures that the context's world's time, reduced modulo {@link #period()} if the period is present, is within
 * {@link #range()}.
 * @param period the (optional) number to reduce modulo the world's time by
 * @param range the range of the modified time
 */
public record TimeRequirement(@Nullable Long period, @NotNull LootNumberRange range) implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, TimeRequirement> CONVERTER = new LootConverter<>("minecraft:time_check", TimeRequirement.class) {

        @Override
        public @NotNull TimeRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            JsonElement period = json.get("period");
            return new TimeRequirement(
                    JsonUtils.isNull(period) ? JsonUtils.assureNumber(period, "period").longValue() : null,
                    LootNumberRange.deserialize(json.get("value"), context)
            );
        }

        @Override
        public void serialize(@NotNull TimeRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            result.addProperty("period", input.period());
            result.add("value", LootNumberRange.serialize(input.range(), context));
        }
    };

    /**
     * @param context the loot context that will be verified
     * @return true if the context's world's time, reduced modulo {@link #period()} if the period is present, fits
     *         within {@link #range()}
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        long time = context.<Instance>assure(LootContextKeys.WORLD).getTime();
        if (period != null) {
            time %= period;
        }
        return this.range.check(context, time);
    }
}
