package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.Trove;
import dev.goldenstack.loot.converter.LootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A loot converter that also stores a TypeToken representing the converted type.
 * @param <V> the converted type
 */
public interface TypedLootConverter<V> extends LootConverter<V> {

    /**
     * Joins the provided type and converter into a new TypedLootConverter.
     * @param type the converted type
     * @param converter the converter to use
     * @return a typed converter joining the provided type and converter
     * @param <V> the converted type
     */
    static <V> @NotNull TypedLootConverter<V> join(@NotNull TypeToken<V> type, @NotNull LootConverter<V> converter) {
        return new TypedLootConverterImpl<>(type, converter);
    }

    /**
     * Joins the provided type and converter into a new TypedLootConverter.
     * @param type the converted type
     * @param converter the converter to use
     * @return a typed converter joining the provided type and converter
     * @param <V> the converted type
     */
    static <V> @NotNull TypedLootConverter<V> join(@NotNull Class<V> type, @NotNull LootConverter<V> converter) {
        return join(TypeToken.get(type), converter);
    }

    /**
     * Returns a type token representing the type that this converter is able to convert.
     * @return the converted type
     */
    @NotNull TypeToken<V> convertedType();

}

record TypedLootConverterImpl<V>(@NotNull TypeToken<V> convertedType, @NotNull LootConverter<V> converter) implements TypedLootConverter<V> {

    @Override
    public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
        converter.serialize(input, result, context);
    }

    @Override
    public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull Trove context) throws ConfigurateException {
        return converter.deserialize(input, context);
    }
}