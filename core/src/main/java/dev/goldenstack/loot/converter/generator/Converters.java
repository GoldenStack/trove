package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.LootDeserializer;
import dev.goldenstack.loot.converter.LootSerializer;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
        return ConvertersImpl.converter(type, List.of(fields));
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
        return ConvertersImpl.converter(type, constructor, fields);
    }

    /**
     * Creates a new field that wraps the provided converter and its type.
     * @param converter the converter to use
     * @return a new field of the provided converter and its type
     * @param <V> the type that the field represents
     */
    public static <V> @NotNull Field<V> field(@NotNull TypedLootConverter<V> converter) {
        return new Field<>(converter, null, null, null);
    }

    /**
     * Creates a field of the provided type.
     * @param type the type of the field
     * @return the created field
     * @param <V> the type that the field represents
     */
    public static <V> @NotNull Field<V> type(@NotNull TypeToken<V> type) {
        return field(TypedLootConverter.join(type,
                (input, result) -> result.set(type, input),
                input -> require(input, type)
        ));
    }

    /**
     * Creates a field of the provided type.
     * @param type the type of the field
     * @return the created field
     * @param <V> the type that the field represents
     */
    public static <V> @NotNull Field<V> type(@NotNull Class<V> type) {
        return type(TypeToken.get(type));
    }

    private static <V> @NotNull V require(@NotNull ConfigurationNode input, @NotNull TypeToken<V> type) throws SerializationException {
        var instance = input.get(type);
        if (instance == null) {
            throw new SerializationException(input, type.getType(), "Cannot coerce node to expected type");
        }
        return instance;
    }

    /**
     * Creates a field of a list of the provided type.
     * @param type the type that the field will be a list of
     * @return the created field
     * @param <V> the type that the field represents
     */
    @SuppressWarnings("unchecked")
    public static <V> @NotNull Field<List<V>> typeList(@NotNull Class<V> type) {
        TypeToken<List<V>> listType = (TypeToken<List<V>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, type));
        return field(TypedLootConverter.join(listType,
                (input, result) -> result.set(input), input -> require(input, listType)));
    }

    /**
     * Creates a field of a list of the provided type.
     * @param type the type that the field will be a list of
     * @return the created field
     * @param <V> the type that the field represents
     */
    @SuppressWarnings("unchecked")
    public static <V> @NotNull Field<List<V>> typePossibleList(@NotNull Class<V> type) {
        TypeToken<List<V>> listType = (TypeToken<List<V>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, type));
        return field(TypedLootConverter.join(listType,
                (input, result) -> result.set(input.size() == 1 ? input.get(0) : input),
                input -> require(input, listType)));
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
        return TypedLootConverter.join(newType, (input, result) -> {
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

    /**
     * Stores the necessary information about a field that is required to convert it on some arbitrary object.
     * @param <V> the actual type of this field
     */
    public static class Field<V> {

        final @NotNull TypedLootConverter<V> converter;
        final @Nullable Supplier<V> defaultValue;
        final @UnknownNullability String localName;
        final @UnknownNullability List<Object> nodePath;

        private Field(@NotNull TypedLootConverter<V> converter, @Nullable Supplier<V> defaultValue,
                      @UnknownNullability String localName, @UnknownNullability List<Object> nodePath) {
            this.converter = converter;
            this.defaultValue = defaultValue;
            this.localName = localName;
            this.nodePath = nodePath;
        }

        /**
         * Updates both names of this field - see {@link #localName(String)} and {@link #nodePath(Object...)} )}.
         * @param name the new local name and node path to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> name(@NotNull String name) {
            return localName(name).nodePath(name);
        }

        /**
         * Sets the name to use locally on the object that will be created.
         * @param name the new local name to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> localName(@NotNull String name) {
            return new Field<>(converter, defaultValue, name, nodePath);
        }

        /**
         * Sets the path that will be used on the configuration node when serializing and deserializing.
         * @param path the new path to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> nodePath(@NotNull List<@NotNull Object> path) {
            return new Field<>(converter, defaultValue, localName, List.copyOf(path));
        }

        /**
         * Sets the path that will be used on the configuration node when serializing and deserializing.
         * @param path the new path to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> nodePath(@NotNull Object @NotNull ... path) {
            return nodePath(List.of(path));
        }

        /**
         * Makes this field use a default value when serializing and deserializing.<br>
         * Be careful when providing a mutable object here, as it may have unexpected consequences.
         * @param defaultValue the supplier of new default values to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> withDefault(@NotNull Supplier<V> defaultValue) {
            return new Field<>(converter, defaultValue, localName, nodePath);
        }

        /**
         * Makes this field use a default value when serializing and deserializing.<br>
         * Be careful when providing a mutable object here, as it may have unexpected consequences.
         * @param defaultValue the new default value to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> withDefault(@NotNull V defaultValue) {
            return withDefault(() -> defaultValue);
        }

        /**
         * Makes this field nullable/optional. This is accomplished by simply setting the default value to null.
         * @return a new field with the updated information
         */
        public @NotNull Field<V> optional() {
            return withDefault(() -> null);
        }

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

class ConvertersImpl {

    static <V> TypedLootConverter<V> converter(@NotNull Class<V> type, @NotNull List<Converters.Field<?>> fields) {
        var constructor = getConstructor(type, fields.stream()
                .map(f -> f.converter)
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
                                               @NotNull List<Converters.Field<?>> fields) {
        for (var field : fields) {
            Objects.requireNonNull(field.localName, "Field must have a local name!");
            Objects.requireNonNull(field.nodePath, "Field must have a node path!");
        }

        LootDeserializer<V> actualDeserializer = input -> {
            Object[] objects = new Object[fields.size()];

            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);
                objects[i] = deserialize(field, input.node(field.nodePath));
            }

            return constructor.construct(objects, input);
        };

        // Store actual fields for the serialization
        java.lang.reflect.Field[] actualFields = new java.lang.reflect.Field[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);

            java.lang.reflect.Field actualField;
            try {
                actualField = type.getDeclaredField(field.localName);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Unknown field '" + field.localName + "' of class '" + type + "'", e);
            }

            if (!actualField.getGenericType().equals(field.converter.convertedType().getType())) {
                throw new RuntimeException("Expected field '" + field.localName + "' of class '" + type + "' to be of type '" + field.converter.convertedType().getType() + "', found '" + actualField.getType() + "'");
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
                    throw new SerializationException(result, field.converter.convertedType().getType(), "Could not retrieve value of field '" + actualFields[i].getName() + "' on type '" + type + "'", e);
                }

                serialize(field, fieldValue, result.node(field.nodePath));
            }
        };

        return TypedLootConverter.join(type, actualSerializer, actualDeserializer);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    private static <V> @Nullable V deserialize(@NotNull Converters.Field<V> field, @NotNull ConfigurationNode input) throws SerializationException {
        if (input.isNull() && field.defaultValue != null) {
            return field.defaultValue.get();
        }
        return field.converter.deserialize(input);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    @SuppressWarnings("unchecked")
    private static <V> void serialize(@NotNull Converters.Field<V> field, @Nullable Object input, @NotNull ConfigurationNode result) throws SerializationException {
        if (input == null) {
            if (field.defaultValue != null) {
                input = field.defaultValue.get();
            }

            // Serialize nothing if the default value is null
            if (input == null) {
                return;
            }
        }
        // This cast is safe because we grab the object directly from the field; it's just that Field#get always returns an object.
        field.converter.serialize((V) input, result);
    }

}
