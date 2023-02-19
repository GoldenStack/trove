package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.util.FallibleFunction;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.function.Supplier;

/**
 * Stores the necessary information about a field that is required to convert it on some arbitrary object.
 * @param type a type token storing the actual type of this field
 * @param converter the converter that converts instances of this field
 * @param localName the local name of this field, used for finding constructor parameters and for finding the actual
 *                  field to read. This can be null, but it's not allowed to be null when passing this field into
 *                  functions like {@link Converters#converter(Class, Field[])}.
 * @param nodeName the node name of this field, used for finding the configuration node that needs to be deserialized
 *                 and for adding the information of instances back onto nodes when serializing. Just like
 *                 {@code localName}, this can be null but should not be if passing into relevant methods.
 * @param defaultValue the default value of this field; used for serialization and deserialization. Be careful when
 *                     providing a mutable object here, as it may have unexpected consequences.
 * @param <T> the actual type of this field
 */
public record Field<T>(@NotNull TypeToken<T> type,
                       @NotNull AdditiveConverter<T> converter,
                       @UnknownNullability String localName, @UnknownNullability String nodeName,
                       @Nullable Supplier<T> defaultValue) {

    /**
     * Creates a new field, given its type and a converter for the aforementioned type.
     * @param type a type token representing the exact type of this field, including parameterized types
     * @param converter the default converter to use for this field
     * @return a new field of the provided type and converter
     * @param <T> the type that the field represents
     */
    public static <T> @NotNull Field<T> field(@NotNull TypeToken<T> type, @NotNull AdditiveConverter<T> converter) {
        return new Field<>(type, converter, null, null, null);
    }

    /**
     * Creates a new field, given its type and a converter for the aforementioned type.<br>
     * Possesses identical semantics to {@link #field(TypeToken, AdditiveConverter)}, except that it automatically
     * converts the class into a type token.<br>
     * <b>This should only be used when the type doesn't have any type arguments; information will be lost if you omit
     * them and provide solely the class.</b>
     * @param type the raw class of the field.
     * @param converter the default converter to use for this field
     * @return a new field of the provided type and converter
     * @param <T> the type that the field represents
     */
    public static <T> @NotNull Field<T> field(@NotNull Class<T> type, @NotNull AdditiveConverter<T> converter) {
        return field(TypeToken.get(type), converter);
    }

    /**
     * Updates both names of this field - see {@link #localName(String)} and {@link #nodeName(String)}.
     * @param name the new local and node name to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> name(@NotNull String name) {
        return new Field<>(type, converter, name, name, defaultValue);
    }

    /**
     * Sets the name to use locally on the object that will be created.
     * @param name the new local name to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> localName(@NotNull String name) {
        return new Field<>(type, converter, name, nodeName, defaultValue);
    }

    /**
     * Sets the name that will be used on the configuration node when serializing and deserializing.
     * @param name the new node name to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> nodeName(@NotNull String name) {
        return new Field<>(type, converter, localName, name, defaultValue);
    }

    /**
     * Makes this field use a default value when serializing and deserializing.<br>
     * Be careful when providing a mutable object here, as it may have unexpected consequences.
     * @param defaultValue the supplier of new default values to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> withDefault(@NotNull Supplier<T> defaultValue) {
        return new Field<>(type, converter, localName, nodeName, defaultValue);
    }

    /**
     * Makes this field use a default value when serializing and deserializing.<br>
     * Be careful when providing a mutable object here, as it may have unexpected consequences.
     * @param defaultValue the new default value to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> withDefault(@NotNull T defaultValue) {
        return withDefault(() -> defaultValue);
    }

    /**
     * Makes this field serialize and deserialize a list of this field's current type.
     * @return a new field with the updated information
     */
    public @NotNull Field<List<T>> list() {
        var oldConverter = converter;

        AdditiveConverter<List<T>> newConverter = Utils.createAdditive(
                (input, result, context) -> Utils.serializeAdditiveList(input, result, oldConverter, context),
                (input, context) -> Utils.deserializeList(input, oldConverter, context)
        );

        @SuppressWarnings("unchecked") // This is safe because TypeFactory.parameterizedClass unfortunately just removes the generic
        TypeToken<List<T>> newType = (TypeToken<List<T>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, this.type.getType()));

        return new Field<>(newType, newConverter, localName, nodeName, null);
    }

    /**
     * Maps this field to a new type with the provided functions.<br>
     * Possesses identical semantics to {@link #map(TypeToken, FallibleFunction, FallibleFunction)}, except that it
     * automatically converts the class into a type token.<br>
     * <b>This should only be used when the type doesn't have any type arguments; information will be lost if you omit
     * them and provide solely the class.</b>
     * @param newType the token of the new type
     * @param toNew the function that maps the old type to the new type
     * @param fromNew the function that maps the new type to the old type
     * @return a new field with the updated information
     * @param <N> the new type
     */
    public <N> @NotNull Field<N> map(@NotNull Class<N> newType,
                                     @NotNull FallibleFunction<T, N> toNew,
                                     @NotNull FallibleFunction<N, T> fromNew) {
        return map(TypeToken.get(newType), toNew, fromNew);
    }

    /**
     * Maps this field to a new type with the provided functions.
     * @param newType the token of the new type
     * @param toNew the function that maps the old type to the new type
     * @param fromNew the function that maps the new type to the old type
     * @return a new field with the updated information
     * @param <N> the new type
     */
    public <N> @NotNull Field<N> map(@NotNull TypeToken<N> newType,
                                     @NotNull FallibleFunction<T, N> toNew,
                                     @NotNull FallibleFunction<N, T> fromNew) {
        var oldConverter = converter;

        AdditiveConverter<N> newConverter = Utils.createAdditive(
                (input, result, context) -> oldConverter.serialize(fromNew.apply(input), result, context),
                (input, context) -> toNew.apply(oldConverter.deserialize(input, context))
        );
        return new Field<>(newType, newConverter, localName, nodeName, null);
    }

}
