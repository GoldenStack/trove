package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConditionalConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.implicit;

/**
 * A constant value that is always returned. When a {@code long} is needed, {@link Math#round(double)} is used.
 * @param value the number to return
 */
public record ConstantNumber(double value) implements LootNumber {

    /**
     * A converter for constant numbers that always serializes to a numerical scalar and deserializes when the input is
     * a singular numerical scalar.
     */
    public static final @NotNull AdditiveConditionalConverter<LootNumber> ACCURATE_CONVERTER = new AdditiveConditionalConverter<>() {
        @Override
        public boolean canSerialize(@NotNull LootNumber input, @NotNull LootConversionContext context) {
            return input instanceof ConstantNumber;
        }

        @Override
        public void serialize(@NotNull LootNumber input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
            result.set(((ConstantNumber) input).value());
        }

        @Override
        public boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) {
            return input.rawScalar() instanceof Number;
        }

        @Override
        public @NotNull LootNumber deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
            return new ConstantNumber(input.require(Double.class));
        }
    };

    /**
     * A standard map-based converter for constant numbers.
     */
    public static final @NotNull KeyedLootConverter<ConstantNumber> CONVERTER =
            converter(ConstantNumber.class,
                    implicit(double.class).name("value")
            ).keyed("minecraft:constant");

    @Override
    public long getLong(@NotNull LootGenerationContext context) {
        return Math.round(value);
    }

    @Override
    public double getDouble(@NotNull LootGenerationContext context) {
        return value;
    }
}
