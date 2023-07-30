package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.LootDeserializer;
import dev.goldenstack.loot.converter.LootSerializer;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.util.FallibleFunction;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Manages the creation of reflective loot converters.
 */
public class Converters {

    /**
     * Creates a typed converter from the provided type and fields. This basically just calls
     * {@link #converter(Class, Constructor, List)} except that the constructor is searched for on the class that
     * is provided.
     * @param type the class of the object that will be converted
     * @param fields the information about fields for this type
     * @return a new typed converter for the provided type
     * @param <V> the type of object that will be converted
     */
    public static <V> @NotNull TypedLootConverter<V> converter(@NotNull Class<V> type, Converters.@NotNull Field<?>... fields) {
        return ConvertersImpl.converter(type, FieldImpl.convert(List.of(fields)));
    }

    /**
     * Creates a typed converter from the provided type, fields, and constructor.
     * @param type the class of the object that will be converted
     * @param constructor the constructor of instances of the type
     * @param fields the information about fields of this type
     * @return a new typed converter for the provided type
     * @param <V> the type of object that will be converted
     */
    public static <V> @NotNull TypedLootConverter<V> converter(@NotNull Class<V> type, @NotNull Constructor<V> constructor,
                                                               @NotNull List<Converters.Field<?>> fields) {
        return ConvertersImpl.converter(type, constructor, FieldImpl.convert(fields));
    }

    /**
     * Creates a new field that wraps the provided converter and its type.
     * @param converter the converter to use
     * @return a new field of the provided converter and its type
     * @param <V> the type that the field represents
     */
    public static <V> @NotNull Field<V> field(@NotNull TypedLootConverter<V> converter) {
        return new FieldImpl<>(converter, null, null, null);
    }

    /**
     * Stores the necessary information about a field that is required to convert it on some arbitrary object.
     * @param <V> the actual type of this field
     */
    public sealed interface Field<V> permits FieldImpl {

        /**
         * Updates both names of this field - see {@link #localName(String)} and {@link #nodePath(Object...)} )}.
         * @param name the new local name and node path to use
         * @return a new field with the updated information
         */
        @NotNull Field<V> name(@NotNull String name);

        /**
         * Sets the name to use locally on the object that will be created.
         * @param name the new local name to use
         * @return a new field with the updated information
         */
        @NotNull Field<V> localName(@NotNull String name);

        /**
         * Sets the path that will be used on the configuration node when serializing and deserializing.
         * @param path the new path to use
         * @return a new field with the updated information
         */
        @NotNull Field<V> nodePath(@NotNull List<@NotNull Object> path);

        /**
         * Sets the path that will be used on the configuration node when serializing and deserializing.
         * @param path the new path to use
         * @return a new field with the updated information
         */
        @NotNull Field<V> nodePath(@NotNull Object @NotNull ... path);

        /**
         * Makes this field use a default value when serializing and deserializing.<br>
         * Be careful when providing a mutable object here, as it may have unexpected consequences.
         * @param defaultValue the supplier of new default values to use
         * @return a new field with the updated information
         */
        @NotNull Field<V> withDefault(@NotNull Supplier<V> defaultValue);

        /**
         * Makes this field nullable/optional. This is accomplished by simply setting the default value to null.
         * @return a new field with the updated information
         */
        @NotNull Field<V> optional();

        /**
         * Makes this field use a default value when serializing and deserializing.<br>
         * Be careful when providing a mutable object here, as it may have unexpected consequences.
         * @param defaultValue the new default value to use
         * @return a new field with the updated information
         */
        @NotNull Field<V> withDefault(@NotNull V defaultValue);

        /**
         * Makes this field serialize and deserialize a list of this field's current type.
         * @return a new field with the updated information
         */
        @NotNull Field<List<V>> list();

        /**
         * Makes this field serialize and deserialize a list of this field's current type, allowing singular elements
         * instead of a list to be treated as a list of one item. For example, if this is deserializing from an object, and
         * it encounters an integer, and is attempting to deserialize lists of integers, it will be treated as deserializing
         * a list containing one integer.
         * @return a new field with the updated information
         */
        @NotNull Field<List<V>> possibleList();

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
        <N> @NotNull Field<N> map(@NotNull Class<N> newType,
                                  @NotNull FallibleFunction<@NotNull V, @Nullable N> toNew,
                                  @NotNull FallibleFunction<@NotNull N, @Nullable V> fromNew);

        /**
         * Maps this field to a new type with the provided functions. If either of the provided functions returns null (i.e.
         * the provided instance could not be converted) an exception will be thrown.
         * @param newType the token of the new type
         * @param toNew the function that maps the old type to the new type
         * @param fromNew the function that maps the new type to the old type
         * @return a new field with the updated information
         * @param <N> the new type
         */
        <N> @NotNull Field<N> map(@NotNull TypeToken<N> newType,
                                  @NotNull FallibleFunction<@NotNull V, @Nullable N> toNew,
                                  @NotNull FallibleFunction<@NotNull N, @Nullable V> fromNew);

    }

    /**
     * Represents a generic constructor that can throw an exception while constructing.
     * @param <V> the constructed type
     */
    public interface Constructor<V> {

        /**
         * Attempts to construct an object from the provided arguments, using the provided node to throw any exceptions
         * as to provide more detailed context.
         * @param arguments the objects to construct with
         * @param node the node to create exceptions with, for increased context
         * @return the constructed object
         */
        @NotNull V construct(Object @NotNull [] arguments, @NotNull ConfigurationNode node) throws SerializationException;

    }

}

record FieldImpl<V>(@NotNull TypedLootConverter<V> converter, @Nullable Supplier<V> defaultValue,
                    @UnknownNullability String localName, @UnknownNullability List<Object> nodePath) implements Converters.Field<V> {

    static @NotNull List<FieldImpl<?>> convert(@NotNull List<Converters.Field<?>> fields) {
        List<FieldImpl<?>> newFields = new ArrayList<>();
        for (var field : fields) {
            newFields.add((FieldImpl<?>) field);
        }
        return newFields;
    }

    public Converters.@NotNull Field<V> name(@NotNull String name) {
        return new FieldImpl<>(converter, defaultValue, name, List.of(name));
    }

    public Converters.@NotNull Field<V> localName(@NotNull String name) {
        return new FieldImpl<>(converter, defaultValue, name, nodePath);
    }

    public Converters.@NotNull Field<V> nodePath(@NotNull List<@NotNull Object> path) {
        return new FieldImpl<>(converter, defaultValue, localName, List.copyOf(path));
    }

    public Converters.@NotNull Field<V> nodePath(@NotNull Object @NotNull ... path) {
        return new FieldImpl<>(converter, defaultValue, localName, List.of(path));
    }

    public Converters.@NotNull Field<V> withDefault(@NotNull Supplier<V> defaultValue) {
        return new FieldImpl<>(converter, defaultValue, localName, nodePath);
    }

    public Converters.@NotNull Field<V> optional() {
        return withDefault(() -> null);
    }

    public Converters.@NotNull Field<V> withDefault(@NotNull V defaultValue) {
        return withDefault(() -> defaultValue);
    }

    public Converters.@NotNull Field<List<V>> list() {
        var oldConverter = converter;

        @SuppressWarnings("unchecked") // This is safe because TypeFactory.parameterizedClass unfortunately just removes the generic
        TypeToken<List<V>> newType = (TypeToken<List<V>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, this.converter.convertedType().getType()));

        TypedLootConverter<List<V>> newConverter = TypedLootConverter.join(newType,
                (input, result) -> {
                    for (var item : input) {
                        oldConverter.serialize(item, result.appendListNode());
                    }
                },
                input -> {
                    if (!input.isList()) {
                        throw new SerializationException(input, newType.getType(), "Expected a list");
                    }

                    List<V> output = new ArrayList<>();
                    for (var child : input.childrenList()) {
                        output.add(oldConverter.deserialize(child));
                    }
                    return output;
                }
        );

        return new FieldImpl<>(newConverter, null, localName, nodePath);
    }

    public Converters.@NotNull Field<List<V>> possibleList() {
        var oldConverter = converter;

        @SuppressWarnings("unchecked") // This is safe because TypeFactory.parameterizedClass unfortunately just removes the generic
        TypeToken<List<V>> newType = (TypeToken<List<V>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, this.converter.convertedType().getType()));

        TypedLootConverter<List<V>> newConverter = TypedLootConverter.join(newType,
                (input, result) -> {
                    if (input.size() == 1) {
                        oldConverter.serialize(input.get(0), result);
                    } else {
                        for (var item : input) {
                            oldConverter.serialize(item, result.appendListNode());
                        }
                    }
                },
                input -> {
                    if (!input.isList()) {
                        return List.of(oldConverter.deserialize(input));
                    }

                    List<V> output = new ArrayList<>();
                    for (var child : input.childrenList()) {
                        output.add(oldConverter.deserialize(child));
                    }
                    return output;
                }
        );

        return new FieldImpl<>(newConverter, null, localName, nodePath);
    }

    public <N> Converters.@NotNull Field<N> map(@NotNull Class<N> newType,
                                                @NotNull FallibleFunction<@NotNull V, @Nullable N> toNew,
                                                @NotNull FallibleFunction<@NotNull N, @Nullable V> fromNew) {
        return map(TypeToken.get(newType), toNew, fromNew);
    }

    public <N> Converters.@NotNull Field<N> map(@NotNull TypeToken<N> newType,
                                                @NotNull FallibleFunction<@NotNull V, @Nullable N> toNew,
                                                @NotNull FallibleFunction<@NotNull N, @Nullable V> fromNew) {
        var oldConverter = converter;

        TypedLootConverter<N> newConverter = TypedLootConverter.join(newType,
                (input, result) -> {
                    var applied = fromNew.apply(input);
                    if (applied == null) {
                        throw new SerializationException(converter.convertedType().getType(), "'" + input + "' could not be serialized or has an invalid type");
                    }
                    oldConverter.serialize(applied, result);
                },
                input -> {
                    var preliminaryObject = oldConverter.deserialize(input);
                    var result = toNew.apply(preliminaryObject);
                    if (result == null) {
                        throw new SerializationException(input, newType.getType(), "'" + preliminaryObject + "' could not be deserialized or has an invalid type");
                    }
                    return result;
                }
        );
        return new FieldImpl<>(newConverter, null, localName, nodePath);
    }

}

class ConvertersImpl {

    static <V> TypedLootConverter<V> converter(@NotNull Class<V> type, @NotNull List<FieldImpl<?>> fields) {
        var constructor = getConstructor(type, fields.stream()
                .map(FieldImpl::converter)
                .map(TypedLootConverter::convertedType)
                .map(TypeToken::getType)
                .map(GenericTypeReflector::erase)
                .toArray(Class[]::new));
        return converter(type, constructor, fields);
    }

    static <V> Converters.@NotNull Constructor<V> getConstructor(@NotNull Class<V> type, @NotNull Class<?>[] fields) {
        Constructor<V> constructor;
        try {
            constructor = type.getDeclaredConstructor(fields);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Unknown constructor for type '" + type + "'", e);
        }

        return (input, node) -> {
            try {
                return constructor.newInstance(input);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new SerializationException(node, type, "Could not create a new instance with constructor '" + constructor + "'", e);
            }
        };
    }

    static <V> TypedLootConverter<V> converter(@NotNull Class<V> type, @NotNull Converters.Constructor<V> constructor,
                                               @NotNull List<FieldImpl<?>> fields) {
        for (var field : fields) {
            Objects.requireNonNull(field.localName(), "Field must have a local name!");
            Objects.requireNonNull(field.nodePath(), "Field must have a node path!");
        }

        LootDeserializer<V> actualDeserializer = input -> {
            Object[] objects = new Object[fields.size()];

            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);
                objects[i] = deserialize(field, input.node(field.nodePath()));
            }

            return constructor.construct(objects, input);
        };

        // Store actual fields for the serialization
        java.lang.reflect.Field[] actualFields = new java.lang.reflect.Field[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);

            java.lang.reflect.Field actualField;
            try {
                actualField = type.getDeclaredField(field.localName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Unknown field '" + field.localName() + "' of class '" + type + "'", e);
            }

            if (!actualField.getGenericType().equals(field.converter().convertedType().getType())) {
                throw new RuntimeException("Expected field '" + field.localName() + "' of class '" + type + "' to be of type '" + field.converter().convertedType().getType() + "', found '" + actualField.getType() + "'");
            }

            if (!actualField.trySetAccessible()) {
                throw new RuntimeException("Could not make field '" + actualField + "' accessible");
            }

            actualFields[i] = actualField;
        }

        LootSerializer<V> actualSerializer = (input, result) -> {
            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);

                Object fieldValue;
                try {
                    fieldValue = actualFields[i].get(input);
                } catch (IllegalAccessException e) {
                    throw new SerializationException(result, field.converter().convertedType().getType(), "Could not retrieve value of field '" + actualFields[i].getName() + "' on type '" + type + "'", e);
                }

                serialize(field, fieldValue, result.node(field.nodePath()));
            }
        };

        return TypedLootConverter.join(type, actualSerializer, actualDeserializer);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    private static <V> @Nullable V deserialize(@NotNull FieldImpl<V> field, @NotNull ConfigurationNode input) throws SerializationException {
        if (input.isNull() && field.defaultValue() != null) {
            return field.defaultValue().get();
        }
        return field.converter().deserialize(input);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    @SuppressWarnings("unchecked")
    private static <V> void serialize(@NotNull FieldImpl<V> field, @Nullable Object input, @NotNull ConfigurationNode result) throws SerializationException {
        if (input == null) {
            if (field.defaultValue() != null) {
                input = field.defaultValue().get();
            }

            // Serialize nothing if the default value is null
            if (input == null) {
                return;
            }
        }
        // This cast is safe because we grab the object directly from the field; it's just that Field#get always returns an object.
        field.converter().serialize((V) input, result);
    }

}
