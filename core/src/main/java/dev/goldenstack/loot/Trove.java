package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides type converters during runtime.
 */
public sealed interface Trove permits TroveImpl {

    /**
     * Returns a valid converter that converts the provided type, or null if there is not one.
     * @param type the desired type to convert
     * @return a valid converter for the desired type, or null if there is not one
     * @param <V> the converted type
     */
    <V> @Nullable TypedLootConverter<V> get(@NotNull Type type);

    /**
     * Returns a valid converter that converts the provided type, or null if there is not one.
     * @param type the desired type to convert
     * @return a valid converter for the desired type, or null if there is not one
     * @param <V> the converted type
     */
    default <V> @Nullable TypedLootConverter<V> get(@NotNull TypeToken<V> type) {
        return this.get(type.getType());
    }

    /**
     * Returns a valid converter that converts the provided type, throwing an exception if there is not one.
     * @param type the desired type to convert
     * @return a valid converter for the desired type
     * @param <V> the converted type
     */
    <V> @NotNull TypedLootConverter<V> require(@NotNull Type type);

    /**
     * Returns a valid converter that converts the provided type, throwing an exception if there is not one.
     * @param type the desired type to convert
     * @return a valid converter for the desired type
     * @param <V> the converted type
     */
    default <V> @NotNull TypedLootConverter<V> require(@NotNull Class<V> type) {
        return require((Type) type);
    }

    /**
     * Returns a valid converter that converts the provided type, throwing an exception if there is not one.
     * @param type the desired type to convert
     * @return a valid converter for the desired type
     * @param <V> the converted type
     */
    default <V> @NotNull TypedLootConverter<V> require(@NotNull TypeToken<V> type) {
        return require(type.getType());
    }

    /**
     * Creates a new Trove builder.
     * @return a new builder
     */
    @Contract(" -> new")
    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private final @NotNull Map<Type, TypedLootConverter<?>> converters = new HashMap<>();

        private Builder() {}

        /**
         * Associates the provided type with the provided converter in this builder.
         * @param convertedType the type of the converter to add
         * @param converter the converter to add
         * @return this, for chaining
         * @param <T> the converted type
         */
        @Contract("_, _ -> this")
        public <T> @NotNull Builder add(@NotNull TypeToken<T> convertedType, @NotNull TypedLootConverter<T> converter) {
            add(TypedLootConverter.join(convertedType, converter));

            return this;
        }

        /**
         * Adds the provided typed converter to this builder.
         * @param converter the typed converter to add
         * @return this, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder add(@NotNull TypedLootConverter<?> converter) {
            if (converters.put(converter.convertedType().getType(), converter) != null) {
                throw new IllegalArgumentException("Type '" + converter.convertedType().getType() + "' is already present in this builder");
            }

            return this;
        }

        /**
         * Builds this builder into a new Trove instance.
         * @return a new Trove object
         */
        @Contract(" -> new")
        public @NotNull Trove build() {
            return new TroveImpl(converters);
        }
    }

}

record TroveImpl(@NotNull Map<Type, TypedLootConverter<?>> converters) implements Trove {

    TroveImpl {
        converters = Map.copyOf(converters);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <V> TypedLootConverter<V> get(@NotNull Type type) {
        var get = converters.get(type);
        return get != null ? (TypedLootConverter<V>) get : null;
    }

    @Override
    public <V> @NotNull TypedLootConverter<V> require(@NotNull Type type) {
        var get = this.<V>get(type);
        if (get != null) {
            return get;
        }
        throw new IllegalArgumentException("Could not find converter for type '" + type + "'");
    }
}
