package dev.goldenstack.loot.serialize.generator;

import dev.goldenstack.loot.serialize.LootDeserializer;
import dev.goldenstack.loot.serialize.LootSerializer;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.get;

/**
 * Manages the creation of reflective serializers.
 */
public class Serializers {

    /**
     * Creates a type serializer from the provided type and fields. This basically just calls
     * {@link #serializer(Class, Constructor, List)} except that the constructor is searched for on the class that
     * is provided.
     * @param type the class of the object that will be serialized
     * @param fields the information about fields for this type
     * @return a new type serializer for the provided type
     * @param <V> the type of object that will be serialized
     */
    public static <V> @NotNull TypeSerializer<V> serializer(@NotNull Class<V> type, Serializers.@NotNull Field<?>... fields) {
        return SerializersImpl.serializer(type, List.of(fields));
    }

    /**
     * Creates a type serializer from the provided type, fields, and constructor.
     * @param type the class of the object that will be serialized
     * @param constructor the constructor of instances of the type
     * @param fields the information about fields of this type
     * @return a new type serializer for the provided type
     * @param <V> the type of object that will be serialized
     */
    public static <V> @NotNull TypeSerializer<V> serializer(@NotNull Class<V> type, @NotNull Constructor<V> constructor,
                                                            @NotNull List<Serializers.Field<?>> fields) {
        return SerializersImpl.serializer(type, constructor, fields);
    }

    /**
     * Creates a field of the provided type.
     * @param type the type of the field
     * @return the created field
     * @param <V> the type that the field represents
     */
    public static <V> @NotNull Field<V> field(@NotNull TypeToken<V> type) {
        return new Field<>().type(type).as(get());
    }

    /**
     * Creates a field of the provided type.
     * @param type the type of the field
     * @return the created field
     * @param <V> the type that the field represents
     */
    public static <V> @NotNull Field<V> field(@NotNull Class<V> type) {
        return field(TypeToken.get(type));
    }

    /**
     * Stores the necessary information about a field that is required to serialize it on some arbitrary object.
     * @param <V> the actual type of this field
     */
    public record Field<V>(@UnknownNullability TypeToken<V> type, @UnknownNullability TypeSerializer<V> serializer, @Nullable Supplier<V> defaultValue,
                           @UnknownNullability String localName, @UnknownNullability List<Object> nodePath) {

        private Field() {
            this(null, null, null, null, null);
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
            return new Field<>(type, serializer, defaultValue, name, nodePath);
        }

        /**
         * Sets the path that will be used on the configuration node when serializing and deserializing.
         * @param path the new path to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> nodePath(@NotNull List<@NotNull Object> path) {
            return new Field<>(type, serializer, defaultValue, localName, List.copyOf(path));
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
         * Sets the type of this field, preserving the name and path.
         * @param type the new type
         * @return a new field with the updated information
         * @param <N> the new type
         */
        public <N> @NotNull Field<N> type(@NotNull TypeToken<N> type) {
            return new Field<>(type, null, null, localName, nodePath);
        }

        /**
         * Sets the type serializer that this field will use.
         * @param serializer this field's new type serializer
         * @return a new field with the updated information
         */
        public @NotNull Field<V> serializer(@NotNull TypeSerializer<V> serializer) {
            return new Field<>(type, serializer, defaultValue, localName, nodePath);
        }

        /**
         * Applies the provided modifier to this field, returning the result.
         * @param modifier the operator that modifies this field
         * @return a new field with the updated information
         * @param <N> the new type of this field
         */
        public <N> @NotNull Field<N> as(@NotNull Function<Field<V>, Field<N>> modifier) {
            return modifier.apply(this);
        }

        /**
         * Makes this field use a default value when serializing and deserializing.<br>
         * Be careful when providing a mutable object here, as it may have unexpected consequences.
         * @param defaultValue the supplier of new default values to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> fallback(@NotNull Supplier<V> defaultValue) {
            return new Field<>(type, serializer, defaultValue, localName, nodePath);
        }

        /**
         * Makes this field use a default value when serializing and deserializing.<br>
         * Be careful when providing a mutable object here, as it may have unexpected consequences.
         * @param defaultValue the new default value to use
         * @return a new field with the updated information
         */
        public @NotNull Field<V> fallback(@NotNull V defaultValue) {
            return fallback(() -> defaultValue);
        }

        /**
         * Makes this field nullable/optional. This is accomplished by simply setting the default value to null.
         * @return a new field with the updated information
         */
        public @NotNull Field<V> optional() {
            return fallback(() -> null);
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

class SerializersImpl {

    static <V> TypeSerializer<V> serializer(@NotNull Class<V> type, @NotNull List<Serializers.Field<?>> fields) {
        var constructor = getConstructor(type, fields.stream()
                .map(Serializers.Field::type)
                .map(TypeToken::getType)
                .map(Objects::requireNonNull)
                .map(GenericTypeReflector::erase)
                .toArray(Class[]::new));
        return serializer(type, constructor, fields);
    }

    static <V> Serializers.@NotNull Constructor<V> getConstructor(@NotNull Class<V> type, @NotNull Class<?>[] fields) {
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

    static <V> @NotNull TypeSerializer<V> serializer(@NotNull Class<V> type, @NotNull Serializers.Constructor<V> constructor,
                                                     @NotNull List<Serializers.Field<?>> fields) {
        for (var field : fields) {
            Objects.requireNonNull(field.type(), "Field must have a type!");
            Objects.requireNonNull(field.serializer(), "Field must have a serializer!");
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

            if (!actualField.getGenericType().equals(field.type().getType())) {
                throw new RuntimeException("Expected field '" + field.localName() + "' of class '" + type + "' to be of type '" + field.type().getType() + "', found '" + actualField.getType() + "'");
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
                    throw new SerializationException(result, field.type().getType(), "Could not retrieve value of field '" + actualFields[i].getName() + "' on type '" + type + "'", e);
                }

                serialize(field, fieldValue, result.node(field.nodePath()));
            }
        };

        return FieldTypes.join(actualSerializer, actualDeserializer);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    private static <V> @Nullable V deserialize(@NotNull Serializers.Field<V> field, @NotNull ConfigurationNode input) throws SerializationException {
        if (input.isNull() && field.defaultValue() != null) {
            return field.defaultValue().get();
        }
        return field.serializer().deserialize(field.type().getType(), input);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    @SuppressWarnings("unchecked")
    private static <V> void serialize(@NotNull Serializers.Field<V> field, @Nullable Object input, @NotNull ConfigurationNode result) throws SerializationException {
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
        field.serializer().serialize(field.type().getType(), (V) input, result);
    }

}
