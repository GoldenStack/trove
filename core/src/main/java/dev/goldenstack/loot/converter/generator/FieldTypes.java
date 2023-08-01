package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.TypedLootConverter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

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

    public static final @NotNull TypeSerializerCollection STANDARD_TYPES = TypeSerializerCollection.builder()
            .register(UUID.class, Converters.proxied(String.class, UUID.class, string -> {
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
