package dev.goldenstack.loot.converter.generator;

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
    private List<Object> keyLocation;
    private final @NotNull List<TypeSerializer<V>> initialSerializers = new ArrayList<>();

    private final @NotNull Map<String, TypeToken<? extends V>> keyToType = new HashMap<>();
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
    public @NotNull LootConversionManager<V> keyLocation(@NotNull Object @NotNull ... keyLocation) {
        this.keyLocation = List.of(keyLocation);
        return this;
    }

    /**
     * Adds a serializer to this builder. These serializers are applied before any types are considered.
     * @param serializer the serializer to add
     * @return this, for chaining
     */
    @Contract("_ -> this")
    public @NotNull LootConversionManager<V> add(@NotNull TypeSerializer<V> serializer) {
        this.initialSerializers.add(serializer);
        return this;
    }

    /**
     * Adds a type under a specific key to this builder. These types are always applied after any serializers are.
     * @param key the key to associate the type with
     * @param type the type to be added
     * @return this, for chaining
     */
    public @NotNull LootConversionManager<V> add(@NotNull String key, @NotNull Class<? extends V> type) {
        return add(key, TypeToken.get(type));
    }

    /**
     * Adds a type under a specific key to this builder. These types are always applied after any serializers are.
     * @param key the key to associate the type with
     * @param type the type to be added
     * @return this, for chaining
     */
    @Contract("_, _ -> this")
    public @NotNull LootConversionManager<V> add(@NotNull String key, @NotNull TypeToken<? extends V> type) {
        if (!GenericTypeReflector.isSuperType(convertedType.getType(), type.getType())) {
            throw new IllegalArgumentException("Converter '" + key + "' has invalid type '" + type.getType() + "' as it is not a subtype of '" + convertedType.getType() + "'");
        } else if (keyToType.put(key, type) != null) {
            throw new IllegalArgumentException("Converter '" + key + "' has a key that has already been registered");
        } else if (typeToKey.put(type, key) != null) {
            throw new IllegalArgumentException("Converter '" + key + "' has a type '" + type.getType() + "' that has already been registered");
        }

        return this;
    }

    /**
     * Builds this builder into a new LootConversionManager instance.
     * @return the new loot conversion manager
     */
    @Contract(" -> new")
    public @NotNull TypeSerializer<V> build() {
        return new LootConversionManagerImpl<>(
                convertedType,
                Objects.requireNonNull(keyLocation, "This builder cannot be built without a key location"),
                List.copyOf(initialSerializers),
                Map.copyOf(keyToType), Map.copyOf(typeToKey)
        );
    }

}

record LootConversionManagerImpl<V>(@NotNull TypeToken<V> convertedType, @NotNull List<Object> keyLocation,
                                    @NotNull List<TypeSerializer<V>> initialSerializers,
                                    @NotNull Map<String, TypeToken<? extends V>> keyToType,
                                    @NotNull Map<TypeToken<? extends V>, String> typeToKey) implements TypeSerializer<V> {
    @Override
    public void serialize(Type type, @Nullable V input, ConfigurationNode result) throws SerializationException {
        if (input == null) {
            throw new SerializationException(result, type, "Cannot serialize null object");
        }
        serialize0(type, input, result);
    }

    private <R extends V> void serialize0(Type type, @NotNull R input, @NotNull ConfigurationNode result) throws SerializationException {
        for (var conditional : initialSerializers) {
            conditional.serialize(type, input, result);
            if (!result.isNull()) {
                return;
            }
        }
        TypeToken<?> token = TypeToken.get(input.getClass());

        String key = typeToKey.get(token);
        if (key == null) {
            throw new SerializationException(result, convertedType.getType(), "Unknown input type '" + input.getClass() + "'");
        }
        result.node(keyLocation).set(key);
        result.set(input);
    }

    @Override
    public V deserialize(Type type, ConfigurationNode input) throws SerializationException {
        for (var conditional : initialSerializers) {
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

        TypeToken<? extends V> subtype = keyToType.get(actualKey);
        if (subtype == null) {
            throw new SerializationException(keyNode, convertedType.getType(), "Unknown key '" + actualKey + "'");
        }
        return input.get(subtype);
    }

}
