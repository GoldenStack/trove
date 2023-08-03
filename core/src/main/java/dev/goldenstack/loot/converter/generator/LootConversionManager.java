package dev.goldenstack.loot.converter.generator;

import dev.goldenstack.loot.converter.TypedLootConverter;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Manages serialization for when multiple subtypes of a base class must be chosen from.
 * @param <V> the base type of object that will be converted
 */
public class LootConversionManager<V> {

    private final @NotNull TypeToken<V> convertedType;
    private String keyLocation;
    private final @NotNull List<TypeSerializer<V>> initialConverters = new ArrayList<>();

    private final @NotNull Map<String, TypedLootConverter<? extends V>> keyToConverter = new HashMap<>();
    private final @NotNull Map<TypeToken<? extends V>, TypedLootConverter<? extends V>> typeToConverter = new HashMap<>();
    private final @NotNull Map<TypeToken<? extends V>, String> typeToKey = new HashMap<>();

    public LootConversionManager(@NotNull TypeToken<V> convertedType) {
        this.convertedType = convertedType;
    }

    /**
     * Sets the location of the key that will be used to determine which typed converter to use.
     * @param keyLocation the location of the key that will be used
     * @return this, for chaining
     */
    @Contract("_ -> this")
    public @NotNull LootConversionManager<V> keyLocation(@NotNull String keyLocation) {
        this.keyLocation = keyLocation;
        return this;
    }

    /**
     * Adds a converter to this builder. These converters are applied before any typed converters are.
     * @param converter the converter to add
     * @return this, for chaining
     */
    @Contract("_ -> this")
    public @NotNull LootConversionManager<V> add(@NotNull TypeSerializer<V> converter) {
        this.initialConverters.add(converter);
        return this;
    }

    /**
     * Adds a typed converter under a specific key to this builder. These typed converters are always applied after
     * any normal converters are.
     * @param key the key to associate the converter with
     * @param converter the converter to be added
     * @return this, for chaining
     */
    @Contract("_, _ -> this")
    public @NotNull LootConversionManager<V> add(@NotNull String key, @NotNull TypedLootConverter<? extends V> converter) {
        if (!GenericTypeReflector.isSuperType(convertedType.getType(), converter.convertedType().getType())) {
            throw new IllegalArgumentException("Converter '" + key + "' has invalid type '" + converter.convertedType().getType() + "' as it is not a subtype of '" + convertedType.getType() + "'");
        } else if (keyToConverter.put(key, converter) != null) {
            throw new IllegalArgumentException("Converter '" + key + "' has a key that has already been registered");
        } else if (typeToConverter.put(converter.convertedType(), converter) != null || typeToKey.put(converter.convertedType(), key) != null) {
            throw new IllegalArgumentException("Converter '" + key + "' has a type '" + converter.convertedType().getType() + "' that has already been registered");
        }

        return this;
    }

    /**
     * Builds this builder into a new LootConversionManager instance.
     * @return the new loot conversion manager
     */
    @Contract(" -> new")
    public @NotNull TypedLootConverter<V> build() {
        return new LootConversionManagerImpl<>(
                convertedType,
                Objects.requireNonNull(keyLocation, "This builder cannot be built without a key location"),
                List.copyOf(initialConverters),
                Map.copyOf(keyToConverter), Map.copyOf(typeToConverter), Map.copyOf(typeToKey)
        );
    }

}

record LootConversionManagerImpl<V>(@NotNull TypeToken<V> convertedType, @NotNull String keyLocation,
                                    @NotNull List<TypeSerializer<V>> initialConverters,
                                    @NotNull Map<String, TypedLootConverter<? extends V>> keyToConverter,
                                    @NotNull Map<TypeToken<? extends V>, TypedLootConverter<? extends V>> typeToConverter,
                                    @NotNull Map<TypeToken<? extends V>, String> typeToKey) implements TypedLootConverter<V> {
    @Override
    public void serialize(Type type, @Nullable V input, ConfigurationNode result) throws SerializationException {
        if (input == null) {
            throw new SerializationException(result, type, "Cannot serialize null object");
        }
        serialize0(type, input, result);
    }

    private <R extends V> void serialize0(Type type, @NotNull R input, @NotNull ConfigurationNode result) throws SerializationException {
        for (var conditional : initialConverters) {
            conditional.serialize(type, input, result);
            if (!result.isNull()) {
                return;
            }
        }
        TypeToken<?> token = TypeToken.get(input.getClass());

        @SuppressWarnings("unchecked")
        TypedLootConverter<R> converter = (TypedLootConverter<R>) typeToConverter.get(token);
        String key = typeToKey.get(token);
        if (converter == null || key == null) {
            throw new SerializationException(result, convertedType.getType(), "Unknown input type '" + input.getClass() + "'");
        }
        result.node(keyLocation).set(key);
        converter.serialize(input.getClass(), input, result);
    }

    @Override
    public V deserialize(Type type, ConfigurationNode input) throws SerializationException {
        // Initial pass with conditional converters
        for (var conditional : initialConverters) {
            var result = conditional.deserialize(type, input);
            if (result != null) {
                return result;
            }
        }
        ConfigurationNode keyNode = input.node(keyLocation);

        String actualKey = keyNode.getString();
        if (actualKey == null) {
            throw new SerializationException(keyNode, convertedType.getType(), "Expected a key in the form of a string");
        }

        TypedLootConverter<? extends V> converter = keyToConverter.get(actualKey);
        if (converter == null) {
            throw new SerializationException(keyNode, convertedType.getType(), "Unknown key '" + actualKey + "'");
        }
        return converter.deserialize(converter.convertedType().getType(), input);
    }

}
