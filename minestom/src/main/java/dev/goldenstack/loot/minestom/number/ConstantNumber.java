package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.serialize.generator.FieldTypes;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A constant value that is always returned.
 * @param value the number to return
 */
public record ConstantNumber(@NotNull Number value) implements LootNumber {

    /**
     * A serializer for constant numbers that always serializes to a numerical scalar and deserializes when the input is
     * a singular numerical scalar.
     */
    public static final @NotNull TypeSerializer<LootNumber> ACCURATE_SERIALIZER = FieldTypes.join(
            (input, result) -> {
                if (input instanceof ConstantNumber constant) {
                    result.set(constant.value());
                }
            }, input -> input.rawScalar() instanceof Number number ? new ConstantNumber(number) : null
    );

    public static final @NotNull String KEY = "minecraft:constant";

    /**
     * A standard map-based serializer for constant numbers.
     */
    public static final @NotNull TypeSerializer<ConstantNumber> SERIALIZER =
            serializer(ConstantNumber.class,
                    field(Number.class).name("value")
            );

    @Override
    public long getLong(@NotNull LootContext context) {
        return value.longValue();
    }

    @Override
    public double getDouble(@NotNull LootContext context) {
        return value.doubleValue();
    }
}
