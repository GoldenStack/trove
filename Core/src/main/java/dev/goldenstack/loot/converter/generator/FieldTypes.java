package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.goldenstack.loot.converter.generator.Field.field;

/**
 * Utility for the creation of various types of fields.
 */
public class FieldTypes {

    /**
     * Creates a field that assumes that any provided configuration node already has a converter for the given type.
     * @param type the type to convert
     * @return a field representing the type
     * @param <T> the type to convert
     */
    public static <T> @NotNull Field<T> implicit(@NotNull Class<T> type) {
        return implicit(TypeToken.get(type));
    }

    /**
     * Creates a field that assumes that any provided configuration node already has a converter for the given type.
     * @param type the type to convert
     * @return a field representing the type
     * @param <T> the type to convert
     */
    public static <T> @NotNull Field<T> implicit(@NotNull TypeToken<T> type) {
        return field(type,
                LootConverter.join(
                    (input, result, context) -> result.set(type, input),
                    (input, context) -> {
                        var instance = input.get(type);
                        if (instance == null) {
                            throw new SerializationException(input, type.getType(), "Cannot coerce node to expected type");
                        }
                        return instance;
                    }
                )
        );
    }

    /**
     * @return a field converting UUIDs
     */
    public static @NotNull Field<UUID> uuid() {
        return implicit(String.class).map(UUID.class, string -> {
            try {
                return UUID.fromString(string);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }, UUID::toString);
    }

    /**
     * Creates a field that converts every enum value from the provided type.
     * @param type the type being converted
     * @param namer the function that gets the names for each value
     * @return a field converting whatever &lt;T&gt; is
     * @param <T> the enumerated type
     */
    public static <T extends Enum<T>> @NotNull Field<T> enumerated(@NotNull Class<T> type, @NotNull Function<T, String> namer) {
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
    public static <T> @NotNull Field<T> enumerated(@NotNull Class<T> type, @NotNull Collection<T> values, @NotNull Function<T, String> namer) {
        Map<String, T> mappings = values.stream().collect(Collectors.toMap(namer, Function.identity()));
        return implicit(String.class).map(type, mappings::get, namer::apply);
    }

    /**
     * @return a field converting loot conditions
     */
    public static @NotNull Field<LootCondition> condition() {
        return loader(LootCondition.class);
    }

    /**
     * @return a field converting loot entries
     */
    public static @NotNull Field<LootEntry> entry() {
        return loader(LootEntry.class);
    }

    /**
     * @return a field converting loot modifiers
     */
    public static @NotNull Field<LootModifier> modifier() {
        return loader(LootModifier.class);
    }

    /**
     * @return a field converting loot numbers
     */
    public static @NotNull Field<LootNumber> number() {
        return loader(LootNumber.class);
    }

    public static <T> @NotNull Field<T> loader(@NotNull Class<T> type) {
        return Field.field(type,
                LootConverter.join(
                        (input, result, context) -> context.loader().requireConverter(type).serialize(input, result, context),
                        (input, context) -> context.loader().requireConverter(type).deserialize(input, context)
                )
        );
    }

}
