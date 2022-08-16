package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.ConditionalLootConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A constant value that is always returned. When a {@code long} is needed, {@link Math#round(double)} is used.
 * @param value the number to return
 */
public record ConstantNumber(double value) implements LootNumber<ItemStack> {

    /**
     * A converter for constant numbers that always serializes to a numerical scalar and deserializes when the input is
     * a singular numerical scalar.
     */
    public static final @NotNull ConditionalLootConverter<ItemStack, LootNumber<ItemStack>> ACCURATE_CONVERTER = new ConditionalLootConverter<>() {
        @Override
        public boolean canSerialize(@NotNull LootNumber<ItemStack> input, @NotNull LootConversionContext<ItemStack> context) {
            return input instanceof ConstantNumber;
        }

        @Override
        public @NotNull ConfigurationNode serialize(@NotNull LootNumber<ItemStack> input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return context.loader().createNode().set(((ConstantNumber) input).value());
        }

        @Override
        public boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) {
            return input.rawScalar() instanceof Number;
        }

        @Override
        public @NotNull LootNumber<ItemStack> deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new ConstantNumber(Utils.require(input, Number.class).doubleValue());
        }
    };

    /**
     * A standard map-based converter for constant numbers.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, ConstantNumber> CONVERTER = Utils.createKeyedConverter("minecraft:constant", new TypeToken<>(){},
            (input, result, context) ->
                    result.node("value").set(input.value()),
            (input, context) -> new ConstantNumber(
                    Utils.require(input.node("value"), Number.class).doubleValue()
            ));

    @Override
    public long getLong(@NotNull LootGenerationContext context) {
        return Math.round(value);
    }

    @Override
    public double getDouble(@NotNull LootGenerationContext context) {
        return value;
    }
}
