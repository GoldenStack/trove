package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.Trove;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Handles loot conversion where the relevant type to convert is retrieved from a key stored on the node, or the type of
 * the object that needs to be serialized.
 * @param <V> the converted type
 */
public interface KeyedLootConverter<V> extends TypedLootConverter<V> {

    /**
     * Creates a new keyed converter using the provided key and typed converter.
     */
    static <T> @NotNull KeyedLootConverter<T> create(@NotNull String key, @NotNull TypedLootConverter<T> converter) {
        return new KeyedLootConverterImpl<>(key, converter);
    }

    /**
     * Returns the key that uniquely (to this type) represents this converter. Basically, this converter will be
     * selected if a configuration node's key, stored at the key location, is equal to this.
     * @return this keyed converter's key
     */
    @NotNull String key();

}

record KeyedLootConverterImpl<T>(@NotNull String key, @NotNull TypedLootConverter<T> converter) implements KeyedLootConverter<T> {

    @Override
    public void serialize(@NotNull T input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
        converter.serialize(input, result, context);
    }

    @Override
    public @NotNull T deserialize(@NotNull ConfigurationNode input, @NotNull Trove context) throws ConfigurateException {
        return converter.deserialize(input, context);
    }

    @Override
    public @NotNull TypeToken<T> convertedType() {
        return converter.convertedType();
    }
}
