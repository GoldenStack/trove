package dev.goldenstack.loot.converter;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

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
        return join(type, converter, converter);
    }

    /**
     * Joins the provided type and converter into a new TypedLootConverter.
     * @param type the converted type
     * @param converter the converter to use
     * @return a typed converter joining the provided type and converter
     * @param <V> the converted type
     */
    static <V> @NotNull TypedLootConverter<V> join(@NotNull Class<V> type, @NotNull LootConverter<V> converter) {
        return join(TypeToken.get(type), converter, converter);
    }

    /**
     * Joins the provided type, serializer, and deserializer into a new TypedLootConverter.
     * @param type the converted type
     * @param serializer the serializer to use
     * @param deserializer the deserializer to use
     * @return a typed converter joining the provided type and converter
     * @param <V> the converted type
     */
    static <V> @NotNull TypedLootConverter<V> join(@NotNull TypeToken<V> type, @NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) {
        return new TypedLootConverterImpl<>(type, serializer, deserializer);
    }

    /**
     * Joins the provided type, serializer, and deserializer into a new TypedLootConverter.
     * @param type the converted type
     * @param serializer the serializer to use
     * @param deserializer the deserializer to use
     * @return a typed converter joining the provided type and converter
     * @param <V> the converted type
     */
    static <V> @NotNull TypedLootConverter<V> join(@NotNull Class<V> type, @NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) {
        return join(TypeToken.get(type), serializer, deserializer);
    }

    /**
     * Returns a type token representing the type that this converter is able to convert.
     * @return the converted type
     */
    @NotNull TypeToken<V> convertedType();

}

record TypedLootConverterImpl<V>(@NotNull TypeToken<V> convertedType, @NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) implements TypedLootConverter<V> {

    @Override
    public void serialize(@NotNull V input, @NotNull ConfigurationNode result) throws SerializationException {
        serializer.serialize(input, result);
    }

    @Override
    public @NotNull V deserialize(@NotNull ConfigurationNode input) throws SerializationException {
        return deserializer.deserialize(input);
    }
}
