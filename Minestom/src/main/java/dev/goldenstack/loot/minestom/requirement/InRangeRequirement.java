package dev.goldenstack.loot.minestom.requirement;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.conversion.LootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.structure.LootRequirement;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Assures that {@link #value()} is inside {@link #range()}
 * @param value the value that must be in the range
 * @param range the range to check the value with
 */
public record InRangeRequirement(@NotNull LootNumber<ItemStack> value, @NotNull LootNumberRange range) implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, InRangeRequirement> CONVERTER = new LootConverter<>("minecraft:value_check", InRangeRequirement.class) {

        @Override
        public @NotNull InRangeRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            return new InRangeRequirement(
                    context.loader().lootNumberManager().deserialize(json.get("value"), context),
                    LootNumberRange.deserialize(json.get("range"), context)
            );
        }

        @Override
        public void serialize(@NotNull InRangeRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            result.add("value", context.loader().lootNumberManager().serialize(input.value(), context));
            result.add("range", LootNumberRange.serialize(input.range(), context));
        }
    };

    /**
     * @param context the loot context that will be verified
     * @return if {@link #value()} is inside {@link #range()}
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        return range.check(context, value.getLong(context));
    }
}
