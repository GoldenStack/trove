package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.util.FallibleFunction;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Stores the necessary information about a field that is required to convert it on some arbitrary object.
 * @param converter the converter that converts instances of this field
 * @param defaultValue the default value of this field; used for serialization and deserialization. Be careful when
 *                     providing a mutable object here, as it may have unexpected consequences.
 * @param localName the local name of this field, used for finding constructor parameters and for finding the actual
 *                  field to read. This can be null, but it's not allowed to be null when passing this field into
 *                  functions like {@link Converters#converter(Class, Field[])}.
 * @param nodePath the node path of this field, used for finding the configuration node that needs to be deserialized
 *                 and for adding the information of instances back onto nodes when serializing. Just like
 *                 {@code localName}, this can be null but should not be if passing into relevant methods.
 * @param <T> the actual type of this field
 */
public record Field<T>(@NotNull TypedLootConverter<T> converter, @Nullable Supplier<T> defaultValue,
                       @UnknownNullability String localName, @UnknownNullability List<Object> nodePath) {

    /**
     * Creates a new field that wraps the provided converter and its type.
     * @param converter the converter to use
     * @return a new field of the provided converter and its type
     * @param <T> the type that the field represents
     */
    public static <T> @NotNull Field<T> field(@NotNull TypedLootConverter<T> converter) {
        return new Field<>(converter, null, null, null);
    }

    public Field {
        if (nodePath != null) {
            nodePath = List.copyOf(nodePath);
        }
    }

    /**
     * Updates both names of this field - see {@link #localName(String)} and {@link #nodePath(Object...)} )}.
     * @param name the new local name and node path to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> name(@NotNull String name) {
        return new Field<>(converter, defaultValue, name, List.of(name));
    }

    /**
     * Sets the name to use locally on the object that will be created.
     * @param name the new local name to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> localName(@NotNull String name) {
        return new Field<>(converter, defaultValue, name, nodePath);
    }

    /**
     * Sets the path that will be used on the configuration node when serializing and deserializing.
     * @param path the new path to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> nodePath(@NotNull List<@NotNull Object> path) {
        return new Field<>(converter, defaultValue, localName, List.copyOf(path));
    }

    /**
     * Sets the path that will be used on the configuration node when serializing and deserializing.
     * @param path the new path to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> nodePath(@NotNull Object @NotNull ... path) {
        return new Field<>(converter, defaultValue, localName, List.of(path));
    }

    /**
     * Makes this field use a default value when serializing and deserializing.<br>
     * Be careful when providing a mutable object here, as it may have unexpected consequences.
     * @param defaultValue the supplier of new default values to use
     * @return a new field with the updated information
     */
    public @NotNull Field<T> withDefault(@NotNull Supplier<T> defaultValue) {
        return new Field<>(converter, defaultValue, localName, nodePath);
    }

    /**
     * Makes this field nullable/optional. This is accomplished by simply setting the default value to null.
     * @return a new field with the updated information
     */
    public @NotNull Field<T> optional() {
        return withDefault(() -> null);
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

        @SuppressWarnings("unchecked") // This is safe because TypeFactory.parameterizedClass unfortunately just removes the generic
        TypeToken<List<T>> newType = (TypeToken<List<T>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, this.converter.convertedType().getType()));

        LootConverter<List<T>> newConverter = LootConverter.join(
                (input, result, context) -> {
                    for (var item : input) {
                        oldConverter.serialize(item, result.appendListNode(), context);
                    }
                },
                (input, context) -> {
                    if (!input.isList()) {
                        throw new SerializationException(input, newType.getType(), "Expected a list");
                    }

                    List<T> output = new ArrayList<>();
                    for (var child : input.childrenList()) {
                        output.add(oldConverter.deserialize(child, context));
                    }
                    return output;
                }
        );

        return new Field<>(TypedLootConverter.join(newType, newConverter), null, localName, nodePath);
    }

    /**
     * Makes this field serialize and deserialize a list of this field's current type, allowing singular elements
     * instead of a list to be treated as a list of one item. For example, if this is deserializing from an object, and
     * it encounters an integer, and is attempting to deserialize lists of integers, it will be treated as deserializing
     * a list containing one integer.
     * @return a new field with the updated information
     */
    public @NotNull Field<List<T>> possibleList() {
        var oldConverter = converter;

        @SuppressWarnings("unchecked") // This is safe because TypeFactory.parameterizedClass unfortunately just removes the generic
        TypeToken<List<T>> newType = (TypeToken<List<T>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, this.converter.convertedType().getType()));

        LootConverter<List<T>> newConverter = LootConverter.join(
                (input, result, context) -> {
                    if (input.size() == 1) {
                        oldConverter.serialize(input.get(0), result, context);
                    } else {
                        for (var item : input) {
                            oldConverter.serialize(item, result.appendListNode(), context);
                        }
                    }
                },
                (input, context) -> {
                    if (!input.isList()) {
                        return List.of(oldConverter.deserialize(input, context));
                    }

                    List<T> output = new ArrayList<>();
                    for (var child : input.childrenList()) {
                        output.add(oldConverter.deserialize(child, context));
                    }
                    return output;
                }
        );

        return new Field<>(TypedLootConverter.join(newType, newConverter), null, localName, nodePath);
    }

    /**
     * Maps this field to a new type with the provided functions.<br>
     * Possesses identical semantics to {@link #map(TypeToken, FallibleFunction, FallibleFunction)}, except that it
     * automatically converts the class into a type token. If either of the provided functions returns null (i.e. the
     * provided instance could not be converted) an exception will be thrown.<br>
     * <b>This should only be used when the type doesn't have any type arguments; information will be lost if you omit
     * them and provide solely the class.</b>
     * @param newType the token of the new type
     * @param toNew the function that maps the old type to the new type
     * @param fromNew the function that maps the new type to the old type
     * @return a new field with the updated information
     * @param <N> the new type
     */
    public <N> @NotNull Field<N> map(@NotNull Class<N> newType,
                                     @NotNull FallibleFunction<@NotNull T, @Nullable N> toNew,
                                     @NotNull FallibleFunction<@NotNull N, @Nullable T> fromNew) {
        return map(TypeToken.get(newType), toNew, fromNew);
    }

    /**
     * Maps this field to a new type with the provided functions. If either of the provided functions returns null (i.e.
     * the provided instance could not be converted) an exception will be thrown.
     * @param newType the token of the new type
     * @param toNew the function that maps the old type to the new type
     * @param fromNew the function that maps the new type to the old type
     * @return a new field with the updated information
     * @param <N> the new type
     */
    public <N> @NotNull Field<N> map(@NotNull TypeToken<N> newType,
                                     @NotNull FallibleFunction<@NotNull T, @Nullable N> toNew,
                                     @NotNull FallibleFunction<@NotNull N, @Nullable T> fromNew) {
        var oldConverter = converter;

        LootConverter<N> newConverter = LootConverter.join(
                (input, result, context) -> {
                    var applied = fromNew.apply(input);
                    if (applied == null) {
                        throw new SerializationException(converter.convertedType().getType(), "'" + input + "' could not be serialized or has an invalid type");
                    }
                    oldConverter.serialize(applied, result, context);
                },
                (input, context) -> {
                    var preliminaryObject = oldConverter.deserialize(input, context);
                    var result = toNew.apply(preliminaryObject);
                    if (result == null) {
                        throw new SerializationException(input, newType.getType(), "'" + preliminaryObject + "' could not be deserialized or has an invalid type");
                    }
                    return result;
                }
        );
        return new Field<>(TypedLootConverter.join(newType, newConverter), null, localName, nodePath);
    }

}
