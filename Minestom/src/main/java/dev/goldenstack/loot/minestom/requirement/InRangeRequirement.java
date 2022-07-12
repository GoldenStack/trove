package dev.goldenstack.loot.minestom.requirement;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.structure.LootRequirement;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Assures that {@link #value()} is inside {@link #range()}
 * @param value the value that must be in the range
 * @param range the range to check the value with
 */
public record InRangeRequirement(@NotNull LootNumber<ItemStack> value, @NotNull LootNumberRange range) implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, InRangeRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:value_check", TypeToken.get(InRangeRequirement.class)) {
        @Override
        public @NotNull InRangeRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new InRangeRequirement(
                    context.loader().lootNumberManager().deserialize(node.node("value"), context),
                    LootNumberRange.deserialize(node.node("range"), context)
            );
        }

        @Override
        public void serialize(@NotNull InRangeRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("value").set(context.loader().lootNumberManager().serialize(input.value(), context));
            result.node("range").set(LootNumberRange.serialize(input.range(), context));
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
