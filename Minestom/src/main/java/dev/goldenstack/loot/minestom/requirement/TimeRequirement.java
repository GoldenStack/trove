package dev.goldenstack.loot.minestom.requirement;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootRequirement;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Assures that the context's world's time, reduced modulo {@link #period()} if the period is present, is within
 * {@link #range()}.
 * @param period the (optional) number to reduce modulo the world's time by
 * @param range the range of the modified time
 */
public record TimeRequirement(@Nullable Long period, @NotNull LootNumberRange range) implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, TimeRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:time_check", TypeToken.get(TimeRequirement.class)) {
        @Override
        public @NotNull TimeRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new TimeRequirement(
                    node.hasChild("period") ? node.node("period").getLong() : null,
                    LootNumberRange.deserialize(node.node("value"), context)
            );
        }

        @Override
        public void serialize(@NotNull TimeRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("period").set(input.period());
            result.node("value").set(LootNumberRange.serialize(input.range(), context));
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
