package dev.goldenstack.loot.converter;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

/**
 * A loot converter that also stores a TypeToken representing the converted type.
 * @param <V> the converted type
 */
public interface TypedLootConverter<V> extends TypeSerializer<V> {

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
     * Joins the provided serializer and deserializer into a LootConverter instance.
     * @param serializer the new converter's serializer
     * @param deserializer the new converter's deserializer
     * @return a converter that joins the provided instances
     * @param <V> the type to convert
     */
    static <V> @NotNull TypeSerializer<V> join(@NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) {
        return new TypeSerializer<>() {
            @Override
            public void serialize(Type type, @Nullable V obj, ConfigurationNode node) throws SerializationException {
                if (obj == null) {
                    throw new SerializationException(node, type, "Cannot serialize null object");
                }
                serializer.serialize(obj, node);
            }

            @Override
            public V deserialize(Type type, ConfigurationNode node) throws SerializationException {
                return deserializer.deserialize(node);
            }
        };
    }

    /**
     * Returns a type token representing the type that this converter is able to convert.
     * @return the converted type
     */
    @NotNull TypeToken<V> convertedType();

}

record TypedLootConverterImpl<V>(@NotNull TypeToken<V> convertedType, @NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) implements TypedLootConverter<V> {

    @Override
    public void serialize(Type type, @Nullable V obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            throw new SerializationException(node, convertedType().getType(), "Cannot serialize null object");
        }
        serializer.serialize(obj, node);
    }

    @Override
    public V deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return deserializer.deserialize(node);
    }

}
