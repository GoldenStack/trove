package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility for the creation of various types of fields.
 */
public class FieldTypes {

    public static final @NotNull TypeSerializerCollection STANDARD_TYPES = FieldTypes.wrap(
            Converters.proxied(String.class, UUID.class, string -> {
                try {
                    return UUID.fromString(string);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }, UUID::toString));

    /**
     * Wraps the provided converters in a new type serializer collection.
     * @param converters the converters to wrap in a type serializer collection
     * @return a type serializer collection representing each provided converter
     */
    public static @NotNull TypeSerializerCollection wrap(@NotNull TypedLootConverter<?> @NotNull ... converters) {
        var builder = TypeSerializerCollection.builder();
        for (var converter : converters) {
            add(converter, builder);
        }
        return builder.build();
    }

    /**
     * Wraps the provided converter in a new type serializer.
     * @param converter the converter to convert to a type serializer
     * @return a type serializer that uses the provided converter
     * @param <V> the converted type
     */
    public static <V> @NotNull TypeSerializer<V> wrapSingular(@NotNull TypedLootConverter<V> converter) {
        return new TypeSerializer<>() {
            @Override
            public V deserialize(Type type, ConfigurationNode node) throws SerializationException {
                return converter.deserialize(node);
            }

            @Override
            public void serialize(Type type, @Nullable V obj, ConfigurationNode node) throws SerializationException {
                if (obj == null) {
                    throw new SerializationException(node, converter.convertedType().getType(), "Cannot serialize null object");
                }
                converter.serialize(obj, node);
            }
        };
    }

    private static <V> void add(@NotNull TypedLootConverter<V> converter, @NotNull TypeSerializerCollection.Builder builder) {
        builder.register(converter.convertedType(), wrapSingular(converter));
    }

    /**
     * Creates a field that converts every enum value from the provided type.
     * @param type the type being converted
     * @param namer the function that gets the names for each value
     * @return a field converting whatever &lt;T&gt; is
     * @param <T> the enumerated type
     */
    public static <T extends Enum<T>> @NotNull TypedLootConverter<T> enumerated(@NotNull Class<T> type, @NotNull Function<T, String> namer) {
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
    public static <T> @NotNull TypedLootConverter<T> enumerated(@NotNull Class<T> type, @NotNull Collection<T> values, @NotNull Function<T, String> namer) {
        Map<String, T> mappings = values.stream().collect(Collectors.toMap(namer, Function.identity()));
        return Converters.proxied(String.class, type, mappings::get, namer);
    }

}
