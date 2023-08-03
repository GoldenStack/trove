package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.TypedLootConverter;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

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
     * Creates a field that converts every enum value from the provided type.
     * @param type the type being converted
     * @param namer the function that gets the names for each value
     * @return a field converting whatever &lt;T&gt; is
     * @param <T> the enumerated type
     */
    public static <T extends Enum<T>> @NotNull TypeSerializer<T> enumerated(@NotNull Class<T> type, @NotNull Function<T, String> namer) {
        return enumerated(type, Arrays.asList(type.getEnumConstants()), namer);
    }

    /**
     * Creates a field that converts every value in the provided collection.
     * @param type the type being converted
     * @param values the list of values being converted
     * @param namer the function that gets the names for each value
     * @return a field converting whatever &lt;T&gt; is
     * @param <T> the enumerated type
     */
    public static <T> @NotNull TypeSerializer<T> enumerated(@NotNull Class<T> type, @NotNull Collection<T> values, @NotNull Function<T, String> namer) {
        Map<String, T> mappings = values.stream().collect(Collectors.toMap(namer, Function.identity()));
        return proxied(String.class, type, mappings::get, namer);
    }

    /**
     * When added to a field, turns it into a list of its previous type.
     */
    public static <V> @NotNull Function<TypedLootConverter<V>, TypedLootConverter<List<V>>> list() {
        return converter -> {
            var type = list(converter.convertedType());
            return TypedLootConverter.join(type, TypedLootConverter.join(
                    (input, result) -> result.set(type),
                    input -> require(input, type)
            ));
        };
    }

    /**
     * When added to a field, turns it into a list of its previous type, but treating a value instead of a list as a
     * list with one item.
     */
    public static <V> @NotNull Function<TypedLootConverter<V>, TypedLootConverter<List<V>>> possibleList() {
        return converter -> {
            var type = list(converter.convertedType());
            return TypedLootConverter.join(type, TypedLootConverter.join(
                    (input, result) -> result.set(input.size() == 1 ? input.get(0) : input),
                    input -> require(input, type)
            ));
        };
    }

    /**
     * Creates a converter that converts type N but internally always converts it to P with the provided methods before
     * interfacing with configuration nodes.
     * @param originalType the original type that interfaces with the node
     * @param newType the type that is converted
     * @param toNew the mapper to the new type
     * @param fromNew the mapper from the new type
     * @return a converter that converts N
     * @param <P> the original type
     * @param <N> the new type
     */
    public static <P, N> @NotNull TypedLootConverter<N> proxied(@NotNull Class<P> originalType, @NotNull Class<N> newType,
                                                                @NotNull Function<@NotNull P, @Nullable N> toNew,
                                                                @NotNull Function<@NotNull N, @Nullable P> fromNew) {
        return TypedLootConverter.join(newType, TypedLootConverter.join((input, result) -> {
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
        ));
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
}
