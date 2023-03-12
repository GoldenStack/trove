package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.LootDeserializer;
import dev.goldenstack.loot.converter.LootSerializer;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.converter.additive.AdditiveLootSerializer;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.util.FallibleFunction;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

/**
 * Manages the creation of reflective loot converters.
 */
public class Converters {

    /**
     * A class that can produce a variety of separate serializers, deserializers, and converters.
     * @param type the type being converted
     * @param deserializer the produced deserializer
     * @param additiveSerializer the produced additive serializer
     * @param <V> the type being used
     */
    public record Producer<V>(@NotNull TypeToken<V> type,
                              @NotNull LootDeserializer<V> deserializer,
                              @NotNull AdditiveLootSerializer<V> additiveSerializer) {

        /**
         * Produces a serializer for this type.
         * @return the produced serializer
         */
        public @NotNull LootSerializer<V> serializer() {
            return additiveSerializer;
        }

        /**
         * Produces a converter for this type.
         * @return the produced converter
         */
        public @NotNull LootConverter<V> converter() {
            return Utils.createConverter(serializer(), deserializer());
        }

        /**
         * Produces an additive converter for this type.
         * @return the produced additive converter
         */
        public @NotNull AdditiveConverter<V> additive() {
            return Utils.createAdditive(additiveSerializer, deserializer);
        }

        /**
         * Produces a keyed loot converter, of the provided key, for this type.
         * @param key the key to use
         * @return the produced keyed loot converter
         */
        public @NotNull KeyedLootConverter<V> keyed(@NotNull String key) {
            return Utils.createKeyedConverter(key, type, additiveSerializer, deserializer);
        }
    }

    /**
     * Creates a converter producer from the provided type and fields. This basically just calls
     * {@link #converter(Class, FallibleFunction, List)} except that the constructor is searched for on the class that
     * is provided.
     * @param type the class of the object that will be converted
     * @param fields the information about fields for this type
     * @return a new converter producer for the provided type
     * @param <V> the type of object that will be converted
     */
    public static <V> @NotNull Producer<V> converter(@NotNull Class<V> type,
                                                     @NotNull Field<?>... fields) {
        List<Field<?>> newFields = List.of(fields);
        Constructor<V> constructor;
        var rawClasses = newFields.stream().map(Field::type).map(TypeToken::getType).map(GenericTypeReflector::erase).toArray(Class[]::new);
        try {
            constructor = type.getDeclaredConstructor(rawClasses);
        } catch (Exception e) {
            throw new RuntimeException("Could not create the deserializer due to the provided exception:", e);
        }
        return converter(type, input -> {
            try {
                return constructor.newInstance(input);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ConfigurateException("(Type: " + type + ") Could not execute custom deserializer '" + constructor + "' via reflection because of an exception:", e);
            }
        }, newFields);
    }

    /**
     * Creates a converter producer from the provided type, fields, and constructor.
     * @param type the class of the object that will be converted
     * @param constructor the constructor of instances of the type
     * @param fields the information about fields of this type
     * @return a new converter producer for the provided type
     * @param <V> the type of object that will be converted
     */
    public static <V> @NotNull Producer<V> converter(@NotNull Class<V> type,
                                                     @NotNull FallibleFunction<Object[], V> constructor,
                                                     @NotNull List<Field<?>> fields) {
        for (var field : fields) {
            Objects.requireNonNull(field.localName(), "Field must have a local name name!");
            Objects.requireNonNull(field.nodeName(), "Field must have a node name name!");
        }

        LootDeserializer<V> actualDeserializer = (input, context) -> {
            Object[] objects = new Object[fields.size()];

            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);
                objects[i] = deserialize(field, input.node(field.nodeName()), context);
            }

            return constructor.apply(objects);
        };

        // Store actual fields for the serialization
        java.lang.reflect.Field[] actualFields = new java.lang.reflect.Field[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);

            java.lang.reflect.Field actualField;
            try {
                actualField = type.getDeclaredField(field.localName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("(Type: " + type + ") Could not get a field with name '" + field.localName() + "':", e);
            }

            if (!actualField.getGenericType().equals(field.type().getType())) {
                throw new RuntimeException("Field '" + field.localName() + "' of type '" + type + "' is not equal to the expected type ('" + field.type() + "')");
            }

            actualFields[i] = actualField;
        }

        AdditiveLootSerializer<V> actualAdditiveSerializer = (input, result, context) -> {
            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);

                Object fieldValue;
                try {
                    fieldValue = actualFields[i].get(input);
                } catch (IllegalAccessException e) {
                    throw new ConfigurateException("(Type: " + type + ") Could not execute serializer because the value of field '" + actualFields[i] + "' could not be retrieved.", e);
                }

                serialize(field, fieldValue, result.node(field.nodeName()), context);
            }
        };

        return new Producer<>(TypeToken.get(type), actualDeserializer, actualAdditiveSerializer);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    private static <T> @Nullable T deserialize(@NotNull Field<T> field, @NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
        if (input.empty()) {
            if (field.defaultValue() == null) {
                throw new ConfigurateException(input, "(Type: " + field.type().getType() + ") Input was empty and this field doesn't have a default value.");
            }
            return field.defaultValue().get();
        }
        return field.converter().deserialize(input, context);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    @SuppressWarnings("unchecked")
    private static <T> void serialize(@NotNull Field<T> field, @Nullable Object input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
        if (input == null) {
            if (field.defaultValue() == null) {
                throw new ConfigurateException("(Type: " + field.type().getType() + ") Input was null and this field doesn't have a default value.");
            }
            input = field.defaultValue().get();

            // Serialize nothing if the default value is null
            if (input == null) {
                return;
            }
        }
        // This cast is safe because we grab the object directly from the field; it's just that Field#get always returns an object.
        field.converter().serialize((T) input, result, context);
    }

}
