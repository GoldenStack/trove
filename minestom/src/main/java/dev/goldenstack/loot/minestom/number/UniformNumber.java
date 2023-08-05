package dev.goldenstack.loot.minestom.number;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Generates a random number between (inclusive) the minimum and maximum. To be precise, for
 * {@link #getLong(LootContext)}, both the minimum and maximum are inclusive, but for
 * {@link #getDouble(LootContext)}, only the minimum is inclusive.<br>
 * The minimum should always be less than the maximum.
 * @param min the minimum value
 * @param max the maximum value
 */
public record UniformNumber(@NotNull LootNumber min, @NotNull LootNumber max) implements LootNumber {

    public static final @NotNull String KEY = "minecraft:uniform";

    /**
     * A standard map-based serializer for uniform numbers.
     */
    public static final @NotNull TypeSerializer<UniformNumber> SERIALIZER =
            serializer(UniformNumber.class,
                    field(LootNumber.class).name("min"),
                    field(LootNumber.class).name("max")
            );

    @Override
    public long getLong(@NotNull LootContext context) {
        return context.random().nextLong(min().getLong(context), max().getLong(context) + 1);
    }

    @Override
    public double getDouble(@NotNull LootContext context) {
        return context.random().nextDouble(min().getDouble(context), max().getDouble(context));
    }
}
