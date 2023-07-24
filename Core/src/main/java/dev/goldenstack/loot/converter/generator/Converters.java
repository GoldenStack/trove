package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.LootDeserializer;
import dev.goldenstack.loot.converter.LootSerializer;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.util.FallibleFunction;
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
     * Creates a converter producer from the provided type and fields. This basically just calls
     * {@link #converter(Class, FallibleFunction, List)} except that the constructor is searched for on the class that
     * is provided.
     * @param type the class of the object that will be converted
     * @param fields the information about fields for this type
     * @return a new converter producer for the provided type
     * @param <V> the type of object that will be converted
     */
    public static <V> @NotNull Producer<V> converter(@NotNull Class<V> type, @NotNull Field<?>... fields) {
        return ConvertersImpl.converter(type, List.of(fields));
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
                                                     @NotNull FallibleFunction<Object @NotNull [], @NotNull V> constructor,
                                                     @NotNull List<Field<?>> fields) {
        return ConvertersImpl.converter(type, constructor, fields);
    }

    /**
     * Produces different types of converters.
     * @param <V> the type to generate converters of
     */
    public sealed interface Producer<V> permits ConvertersImpl.ProducerImpl {

        /**
         * Produces a loot converter that converts this type.
         * @return the produced converter
         */
        @NotNull LootConverter<V> converter();

        /**
         * Produces a keyed loot converter with the provided key that converts this type.
         * @param key the key to use
         * @return the produced keyed loot converter
         */
        @NotNull KeyedLootConverter<V> keyed(@NotNull String key);

    }

}

class ConvertersImpl {

    static <V> Converters.Producer<V> converter(@NotNull Class<V> type, @NotNull List<Field<?>> fields) {
        var constructor = getConstructor(type, fields.stream().map(Field::type).map(TypeToken::getType).map(GenericTypeReflector::erase).toArray(Class[]::new));
        return converter(type, constructor, fields);
    }

    static <V> @NotNull FallibleFunction<Object @NotNull [], @NotNull V> getConstructor(@NotNull Class<V> type, @NotNull Class<?>[] fields) {
        Constructor<V> constructor;
        try {
            constructor = type.getDeclaredConstructor(fields);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Unknown constructor for type '" + type + "'", e);
        }

        return input -> {
            try {
                return constructor.newInstance(input);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ConfigurateException("Could not run deserializer '" + constructor + "' on type '" + type + "'", e);
            }
        };
    }

    static <V> Converters.Producer<V> converter(@NotNull Class<V> type,
                                                @NotNull FallibleFunction<Object @NotNull [], @NotNull V> constructor,
                                                @NotNull List<Field<?>> fields) {
        for (var field : fields) {
            Objects.requireNonNull(field.localName(), "Field must have a local name!");
            Objects.requireNonNull(field.nodePath(), "Field must have a node path!");
        }

        LootDeserializer<V> actualDeserializer = (input, context) -> {
            Object[] objects = new Object[fields.size()];

            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);
                objects[i] = deserialize(field, input.node(field.nodePath()), context);
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
                throw new RuntimeException("Unknown field '" + field.localName() + "' of class '" + type + "'", e);
            }

            if (!actualField.getGenericType().equals(field.type().getType())) {
                throw new RuntimeException("Expected field '" + field.localName() + "' of class '" + type + "' to be of type '" + field.type() + "', found '" + actualField.getType() + "'");
            }

            actualFields[i] = actualField;
        }

        LootSerializer<V> actualSerializer = (input, result, context) -> {
            for (int i = 0; i < fields.size(); i++) {
                var field = fields.get(i);

                Object fieldValue;
                try {
                    fieldValue = actualFields[i].get(input);
                } catch (IllegalAccessException e) {
                    throw new ConfigurateException("Could not retrieve value of field '" + actualFields[i].getName() + "' on type '" + type + "'", e);
                }

                serialize(field, fieldValue, result.node(field.nodePath()), context);
            }
        };

        return new ConvertersImpl.ProducerImpl<>(TypeToken.get(type), LootConverter.join(actualSerializer, actualDeserializer));
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    private static <T> @Nullable T deserialize(@NotNull Field<T> field, @NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
        if (input.isNull() && field.defaultValue() != null) {
            return field.defaultValue().get();
        }
        return field.converter().deserialize(input, context);
    }

    // Used to store a constant type parameter so that we don't have conflicting type arguments that appear identical
    @SuppressWarnings("unchecked")
    private static <T> void serialize(@NotNull Field<T> field, @Nullable Object input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
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
        field.converter().serialize((T) input, result, context);
    }

    record ProducerImpl<V>(@NotNull TypeToken<V> type, @NotNull LootConverter<V> converter) implements Converters.Producer<V> {

        @Override
        public @NotNull KeyedLootConverter<V> keyed(@NotNull String key) {
            return KeyedLootConverter.create(key, type, converter);
        }
    }

}
