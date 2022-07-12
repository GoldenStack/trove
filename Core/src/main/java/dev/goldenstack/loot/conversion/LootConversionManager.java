package dev.goldenstack.loot.conversion;

import dev.goldenstack.loot.context.LootConversionContext;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

/**
 * Manages serialization and deserialization ("conversion") for groups of classes that all stem from the same source.
 * @param <L> the loot item
 * @param <T> the base class that will be serialized and deserialized
 */
public class LootConversionManager<L, T extends LootAware<L>> {

    private final @NotNull TypeToken<T> baseType;
    private final @NotNull String keyLocation;

    private final @NotNull Map<String, KeyedLootConverter<L, ? extends T>> keyRegistry;
    private final @NotNull Map<TypeToken<? extends T>, KeyedLootConverter<L, ? extends T>> typeTokenRegistry;

    private final @NotNull List<ComplexLootConverter<L, T>> complexConverters;

    private LootConversionManager(@NotNull Builder<L, T> builder) {
        this.baseType = builder.baseType;
        this.keyLocation = builder.keyLocation;
        this.keyRegistry = Map.copyOf(builder.keyRegistry);
        this.typeTokenRegistry = Map.copyOf(builder.classRegistry);
        this.complexConverters = List.copyOf(builder.complexConverters);
    }

    public @NotNull TypeToken<T> baseType() {
        return baseType;
    }

    /**
     * @return the location in nodes at which keys will be searched for
     */
    public @NotNull String keyLocation() {
        return keyLocation;
    }

    /**
     * @param key the key to get from this manager's internal map
     * @return the converter that is associated with the provided key
     */
    public @Nullable KeyedLootConverter<L, ? extends T> request(@NotNull String key) {
        return keyRegistry.get(key);
    }

    /**
     * Serializes the provided object into a configuration node.
     * @param s the object to serialize
     * @param context the context to feed to converters for serialization
     * @return a configuration node representing the serialized state of the provided object
     * @param <S> the type of the object that will be serialized
     * @throws ConfigurateException if something happens during serialization or if a valid loot converter couldn't be found
     */
    public <S extends T> @NotNull ConfigurationNode serialize(@NotNull S s, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        if (!complexConverters.isEmpty()) {
            for (ComplexLootConverter<L, T> complexConverter : complexConverters) {
                if (complexConverter.canSerialize(s, context)) {
                    return complexConverter.serialize(s, context);
                }
            }
        }
        @SuppressWarnings("unchecked") // We, at this point, know that it must be a KeyedLootConverter<L, S>.
        KeyedLootConverter<L, S> converter = (KeyedLootConverter<L, S>) this.typeTokenRegistry.get(TypeToken.get(s.getClass()));
        if (converter == null) {
            throw new ConfigurateException("Could not find a KeyedLootConverter for class '" + s.getClass() + "'");
        }
        ConfigurationNode node = context.loader().createNode();
        node.node(this.keyLocation).set(converter.key());
        converter.serialize(s, node, context);
        return node;
    }

    /**
     * Deserializes the provided node into an instance of something that extends {@link T}.
     * @param node the node to deserialize
     * @param context the context to feed to converters for deserialization
     * @return the instance of something extending {@code T} that was deserialized
     * @throws ConfigurateException if something happens during deserialization or if a valid key couldn't be found in the node
     */
    public @NotNull T deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        if (!complexConverters.isEmpty()) {
            for (ComplexLootConverter<L, T> complexConverter : complexConverters) {
                if (complexConverter.canDeserialize(node, context)) {
                    return complexConverter.deserialize(node, context);
                }
            }
        }
        ConfigurationNode keyLocationNode = node.node(keyLocation);
        String type = keyLocationNode.getString();
        if (type == null) {
            throw new ConfigurateException(keyLocationNode, "Expected a string at the key location");
        }
        KeyedLootConverter<L, ? extends T> t = this.keyRegistry.get(type);
        if (t == null) {
            // todo
            throw new ConfigurateException(keyLocationNode, "Could not find deserializer for type '" + type + "'");
        }
        return t.deserialize(node, context);
    }

    /**
     * @return a new builder for manager instances
     * @param <L> the loot item
     * @param <T> the base class that will be serialized and deserialized
     */
    @Contract(" -> new")
    public static <L, T extends LootAware<L>> @NotNull Builder<L, T> builder() {
        return new Builder<>();
    }

    /**
     * Utility class for creating {@link LootConversionManager} instances.
     * @param <L> the loot item
     * @param <T> the base class that will be serialized and deserialized
     */
    public static final class Builder<L, T extends LootAware<L>> {

        private TypeToken<T> baseType = new TypeToken<>(){};
        private String keyLocation;
        private final @NotNull Map<String, KeyedLootConverter<L, ? extends T>> keyRegistry = new HashMap<>();
        private final @NotNull Map<TypeToken<? extends T>, KeyedLootConverter<L, ? extends T>> classRegistry = new HashMap<>();
        private final @NotNull List<ComplexLootConverter<L, T>> complexConverters = new ArrayList<>();

        private Builder() {}

        /**
         * @param baseType the base type that everything registered must use or extend
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L, T> baseType(@NotNull TypeToken<T> baseType) {
            this.baseType = baseType;
            return this;
        }

        /**
         * @param keyLocation the new location of the {@link KeyedLootConverter#key()} for managers built with this builder
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L, T> keyLocation(@NotNull String keyLocation) {
            this.keyLocation = keyLocation;
            return this;
        }

        /**
         * @param converter the converter to register to any built managers
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L, T> addConverter(@NotNull KeyedLootConverter<L, ? extends T> converter) {
            if (this.keyRegistry.containsKey(converter.key())) {
                throw new IllegalArgumentException("Cannot register value for key '" + converter.key() + "' as something with that key is already registered");
            }
            if (this.classRegistry.containsKey(converter.typeToken())) {
                throw new IllegalArgumentException("Cannot register value for class '" + converter.typeToken() + "' as something with that class is already registered");
            }
            this.keyRegistry.put(converter.key(), converter);
            this.classRegistry.put(converter.typeToken(), converter);
            return this;
        }

        /**
         * @param converter the (complex) converter to register to any built managers
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L, T> addComplexConverter(@NotNull ComplexLootConverter<L, T> converter) {
            this.complexConverters.add(converter);
            return this;
        }

        /**
         * Note: it is safe to build this builder multiple times, but it is not recommended to do so.
         * @return a new {@code LootConversionManager} instance created from this builder.
         */
        @Contract(" -> new")
        public @NotNull LootConversionManager<L, T> build() {
            Objects.requireNonNull(baseType, "LootConversionManager instances cannot be built without a base type!");
            Objects.requireNonNull(keyLocation, "LootConversionManager instances cannot be built without a key location!");
            return new LootConversionManager<>(this);
        }
    }

}
