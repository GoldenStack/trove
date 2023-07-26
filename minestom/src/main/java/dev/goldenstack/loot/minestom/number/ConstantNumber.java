package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.Trove;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.ConditionalLootConverter;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
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
    public static final @NotNull ConditionalLootConverter<LootNumber> ACCURATE_CONVERTER = new ConditionalLootConverter<>() {
        @Override
        public boolean canSerialize(@NotNull LootNumber input, @NotNull Trove loader) {
            return input instanceof ConstantNumber;
        }

        @Override
        public void serialize(@NotNull LootNumber input, @NotNull ConfigurationNode result, @NotNull Trove loader) throws ConfigurateException {
            result.set(((ConstantNumber) input).value());
        }

        @Override
        public boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull Trove loader) {
            return input.rawScalar() instanceof Number;
        }

        @Override
        public @NotNull LootNumber deserialize(@NotNull ConfigurationNode input, @NotNull Trove loader) throws ConfigurateException {
            return new ConstantNumber(input.require(Double.class));
        }
    };

    public static final @NotNull String KEY = "minecraft:constant";

    /**
     * A standard map-based converter for constant numbers.
     */
    public static final @NotNull TypedLootConverter<ConstantNumber> CONVERTER =
            converter(ConstantNumber.class,
                    implicit(double.class).name("value")
            );

    @Override
    public long getLong(@NotNull LootContext context) {
        return Math.round(value);
    }

    @Override
    public double getDouble(@NotNull LootContext context) {
        return value;
    }
}
