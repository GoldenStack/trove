package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Handles loot conversion where the relevant type to convert is retrieved from a key stored on the node, or the type of
 * the object that needs to be serialized.
 * @param <T> the converted type
 */
public interface KeyedLootConverter<T> extends AdditiveConverter<T> {

    /**
     * Creates a new additive converter using the provided key, type, and converter.
     */
    static <T> @NotNull KeyedLootConverter<T> create(@NotNull String key, @NotNull TypeToken<T> convertedType,
                                                     @NotNull AdditiveConverter<T> converter) {
        return new KeyedLootConverterImpl<>(key, convertedType, converter);
    }

    /**
     * Returns the key that uniquely (to this type) represents this converter. Basically, this converter will be
     * selected if a configuration node's key, stored at the key location, is equal to this.
     * @return this keyed converter's key
     */
    @NotNull String key();

    /**
     * Returns the exact type of object that is required as input. Not even subclasses are allowed.
     * @return this keyed converter's type
     */
    @NotNull TypeToken<T> convertedType();

}

record KeyedLootConverterImpl<T>(@NotNull String key, @NotNull TypeToken<T> convertedType, @NotNull AdditiveConverter<T> converter) implements KeyedLootConverter<T> {

    @Override
    public void serialize(@NotNull T input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
        converter.serialize(input, result, context);
    }

    @Override
    public @NotNull T deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
        return converter.deserialize(input, context);
    }
}
