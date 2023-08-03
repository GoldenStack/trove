package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * A constant value that is always returned. When a {@code long} is needed, {@link Math#round(double)} is used.
 * @param value the number to return
 */
public record ConstantNumber(double value) implements LootNumber {

    /**
     * A converter for constant numbers that always serializes to a numerical scalar and deserializes when the input is
     * a singular numerical scalar.
     */
    public static final @NotNull TypeSerializer<LootNumber> ACCURATE_CONVERTER = TypedLootConverter.join(
            (input, result) -> {
                if (input instanceof ConstantNumber constant) {
                    result.set(constant.value());
                }
            }, input -> {
                if (input.rawScalar() instanceof Number number) {
                    return new ConstantNumber(number.doubleValue());
                }
                return null;
            }
    );

    public static final @NotNull String KEY = "minecraft:constant";

    /**
     * A standard map-based converter for constant numbers.
     */
    public static final @NotNull TypedLootConverter<ConstantNumber> CONVERTER =
            converter(ConstantNumber.class,
                    field(double.class).name("value")
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
