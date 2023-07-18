package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.additive.AdditiveConditionalConverter;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

/**
 * Manages serialization for when multiple subtypes of a base class must be chosen from.
 * @param <V> the base type of object that will be converted
 */
public class LootConversionManager<V> {

    private final @NotNull TypeToken<V> baseType;
    private final @NotNull String keyLocation;
    private final @NotNull List<AdditiveConditionalConverter<V>> initialConverters;
    private final @NotNull Map<String, KeyedLootConverter<? extends V>> directKeyRegistry;
    private final @NotNull Map<TypeToken<? extends V>, KeyedLootConverter<? extends V>> typeTokenRegistry;

    private LootConversionManager(@NotNull Builder<V> builder) {
        this.baseType = Objects.requireNonNull(builder.baseType, "This builder cannot be built without a base type");
        this.keyLocation = Objects.requireNonNull(builder.keyLocation, "This builder cannot be built without a key location");

        Map<String, KeyedLootConverter<? extends V>> directKeys = new HashMap<>();
        Map<TypeToken<? extends V>, KeyedLootConverter<? extends V>> typeTokens = new HashMap<>();
        for (var converter : builder.keyedConverters) {
            if (!GenericTypeReflector.isSuperType(baseType.getType(), converter.convertedType().getType())) {
                throw new IllegalArgumentException("Converter '" + converter.key() + "' has invalid type '" + converter.convertedType().getType() + "' as it is not a subtype of '" + baseType.getType() + "'");
            }
            if (directKeys.put(converter.key(), converter) != null) {
                throw new IllegalArgumentException("Converter '" + converter.key() + "' has a key that has already been registered");
            }
            if (typeTokens.put(converter.convertedType(), converter) != null) {
                throw new IllegalArgumentException("Converter '" + converter.key() + "' has a type '" + converter.convertedType().getType() + "' that has already been registered");
            }
        }

        this.directKeyRegistry = Map.copyOf(directKeys);
        this.typeTokenRegistry = Map.copyOf(typeTokens);
        this.initialConverters = List.copyOf(builder.initialConverters);
    }

    /**
     * This is the base type of this manager. It's the input type for serializers and the output type for deserializers.
     * For {@link AdditiveConditionalConverter}s, it's the exact parameter for both, but subtypes of it are allowed (due
     * to polymorphism) for the actual values of them. For {@link KeyedLootConverter}s, the exact parameter type must be
     * a subtype of this base type.
     * @return the base type that all conditional converters must handle and that all keyed converters must handle a
     *         subtype of
     */
    public @NotNull TypeToken<V> baseType() {
        return baseType;
    }

    /**
     * Serializes the provided object (that is a subtype of {@link V}) into a configuration node.<br>
     * As it may not be self-explanatory, here's specifically how the process works:<br>
     * All of the initial converters (that were added to the builder) are each checked, in the order they were added to
     * the aforementioned border, to see if they will serialize the provided input. If any one of them does, it is used
     * to serialize the input and the result is returned. These basically function as an extremely customizable
     * alternative to keyed loot converters.<br>
     * Otherwise, this manager looks for the keyed converter that has a {@link KeyedLootConverter#convertedType()} equal
     * to the type of the input and uses that to serialize it. If this process couldn't be done for any reason, an
     * exception explaining why is thrown.
     * @param input the instance of a subtype of {@link V} to serialize into a configuration node
     * @param context the context object, to use if required
     * @param <R> the real type of {@code input}
     * @throws ConfigurateException if the input could not be serialized for some reason
     */
    public <R extends V> void serialize(@NotNull R input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
        if (!initialConverters.isEmpty()) {
            for (var conditional : initialConverters) {
                if (conditional.canSerialize(input, context)) {
                    conditional.serialize(input, result, context);
                    return;
                }
            }
        }
        @SuppressWarnings("unchecked")
        KeyedLootConverter<R> converter = (KeyedLootConverter<R>) typeTokenRegistry.get(TypeToken.get(input.getClass()));
        if (converter == null) {
            throw new ConfigurateException("Unknown input type '" + input.getClass() + "' for base type '" + baseType.getType() + "'");
        }
        result.node(keyLocation).set(converter.key());
        converter.serialize(input, result, context);
    }

    /**
     * Deserializes the provided configuration node into an instance of (or an instance of a subtype of) {@link V}.<br>
     * As it may not be self-explanatory, here's specifically how the process works:<br>
     * All of the initial converters (that were added to the builder) are each checked, in the order they were added to
     * the aforementioned border, to see if they will deserialize the provided input. If any one of them does, it is
     * used to deserialize the input and the result is returned. These basically function as an extremely customizable
     * alternative to keyed loot converters.<br>
     * Otherwise, the input's child at the key location is used to determine which keyed converter to use, and then the
     * converter is used to deserialize the input. If this process couldn't be done for any reason, an exception
     * explaining why is thrown.
     * @param input the configuration node to deserialize into a {@link V}
     * @param context the context object, to use if required
     * @return the deserialized version of the provided input
     * @throws ConfigurateException if the input could not be deserialized for some reason
     */
    public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
        if (!initialConverters.isEmpty()) {
            for (var conditional : initialConverters) {
                if (conditional.canDeserialize(input, context)) {
                    return conditional.deserialize(input, context);
                }
            }
        }
        ConfigurationNode keyNode = input.node(keyLocation);

        String actualKey = keyNode.getString();
        if (actualKey == null) {
            throw new SerializationException(keyNode, String.class, "Expected a key");
        }

        KeyedLootConverter<? extends V> converter = directKeyRegistry.get(actualKey);
        if (converter == null) {
            throw new ConfigurateException(keyNode, "Unknown key '" + actualKey + "' for base type '" + baseType().getType() + "'");
        }
        return converter.deserialize(input, context);
    }

    /**
     * Creates a new builder for this class, with empty lists of converters and all other fields as null.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new LootConversionManager builder
     * @param <V> the base type of converted objects
     */
    @Contract(" -> new")
    public static <V> @NotNull Builder<V> builder() {
        return new Builder<>();
    }

    public static final class Builder<V> {

        private TypeToken<V> baseType;
        private String keyLocation;
        private final @NotNull List<KeyedLootConverter<? extends V>> keyedConverters = new ArrayList<>();
        private final @NotNull List<AdditiveConditionalConverter<V>> initialConverters = new ArrayList<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder<V> baseType(@NotNull TypeToken<V> baseType) {
            this.baseType = baseType;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<V> keyLocation(@NotNull String keyLocation) {
            this.keyLocation = keyLocation;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<V> addConverter(@NotNull KeyedLootConverter<? extends V> converter) {
            this.keyedConverters.add(converter);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<V> addInitialConverter(@NotNull AdditiveConditionalConverter<V> converter) {
            this.initialConverters.add(converter);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootConversionManager<V> build() {
            return new LootConversionManager<>(this);
        }
    }

}
