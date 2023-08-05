package dev.goldenstack.loot.serialize.generator;

import dev.goldenstack.loot.serialize.LootDeserializer;
import dev.goldenstack.loot.serialize.LootSerializer;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility for the creation of various types of fields.
 */
public class FieldTypes {

    public static final @NotNull TypeSerializerCollection STANDARD_TYPES = TypeSerializerCollection.builder()
            .register(UUID.class, proxied(String.class, UUID.class, string -> {
                try {
                    return UUID.fromString(string);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }, UUID::toString)).build();

    /**
     * Creates a field that serializes every enum value from the provided type.
     * @param type the type being serialized
     * @param namer the function that gets the names for each value
     * @return a field serializing whatever &lt;T&gt; is
     * @param <T> the enumerated type
     */
    public static <T extends Enum<T>> @NotNull TypeSerializer<T> enumerated(@NotNull Class<T> type, @NotNull Function<T, String> namer) {
        return enumerated(type, Arrays.asList(type.getEnumConstants()), namer);
    }

    /**
     * Creates a field that serializes every value in the provided collection.
     * @param type the type being serialized
     * @param values the list of values being serialized
     * @param namer the function that gets the names for each value
     * @return a field serializing whatever &lt;T&gt; is
     * @param <T> the enumerated type
     */
    public static <T> @NotNull TypeSerializer<T> enumerated(@NotNull Class<T> type, @NotNull Collection<T> values, @NotNull Function<T, String> namer) {
        Map<String, T> mappings = values.stream().collect(Collectors.toMap(namer, Function.identity()));
        return proxied(String.class, type, mappings::get, namer);
    }

    /**
     * When applied to a field, sets its serializer to simply using #set and #get methods.
     */
    public static <V> @NotNull Function<Serializers.Field<V>, Serializers.Field<V>> get() {
        return field -> field.serializer(join(
                (input, result) -> result.set(input),
                input -> require(input, field.type())
        ));
    }

    /**
     * When applied to a field, turns it into a list of its previous type.
     */
    public static <V> @NotNull Function<Serializers.Field<V>, Serializers.Field<List<V>>> list() {
        return field -> field.type(list(field.type())).as(get());
    }

    /**
     * When applied to a field, turns it into a list of its previous type, but treating a value instead of a list as a
     * list with one item.
     */
    public static <V> @NotNull Function<Serializers.Field<V>, Serializers.Field<List<V>>> possibleList() {
        return field -> {
            var type = list(field.type());
            return field.type(type).serializer(join(
                    (input, result) -> result.set(input.size() == 1 ? input.get(0) : input),
                    input -> require(input, type)
            ));
        };
    }

    /**
     * Creates a serializer that uses type N but internally always converts it to P with the provided functions before
     * interfacing with configuration nodes.
     * @param originalType the original type that interfaces with the node
     * @param newType the type that is serialized
     * @param toNew the mapper to the new type
     * @param fromNew the mapper from the new type
     * @return a serializer that serializes N
     * @param <P> the original type
     * @param <N> the new type
     */
    public static <P, N> @NotNull TypeSerializer<N> proxied(@NotNull Class<P> originalType, @NotNull Class<N> newType,
                                                                @NotNull Function<@NotNull P, @Nullable N> toNew,
                                                                @NotNull Function<@NotNull N, @Nullable P> fromNew) {
        return join((input, result) -> {
                var applied = fromNew.apply(input);
                if (applied == null) {
                    throw new SerializationException(originalType, "'" + input + "' could not be serialized or has an invalid type");
                }
                result.set(originalType, applied);
            }, input -> {
                var preliminaryObject = require(input, TypeToken.get(originalType));
                var result = toNew.apply(preliminaryObject);
                if (result == null) {
                    throw new SerializationException(input, newType, "'" + preliminaryObject + "' could not be deserialized or has an invalid type");
                }
                return result;
            }
        );
    }

    @SuppressWarnings("unchecked")
    private static <V> @NotNull TypeToken<List<V>> list(@NotNull TypeToken<V> token) {
        return (TypeToken<List<V>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, token.getType()));
    }

    public static <V> @NotNull V require(@NotNull ConfigurationNode input, @NotNull TypeToken<V> type) throws SerializationException {
        var instance = input.get(type);
        if (instance == null) {
            throw new SerializationException(input, type.getType(), "Cannot coerce node to expected type");
        }
        return instance;
    }

    /**
     * Joins the provided serializer and deserializer into a type serializer.
     * @param serializer the new type serializer's serializer
     * @param deserializer the new type serializer's deserializer
     * @return a new type serializer that joins the provided instances
     * @param <V> the type to serialize
     */
    public static <V> @NotNull TypeSerializer<V> join(@NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) {
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
}
