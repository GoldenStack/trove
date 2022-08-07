package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

/**
 * Manages serialization for when multiple subtypes of a base class must be chosen from.
 * @param <L> the loot item type
 * @param <V> the base type of object that will be converted
 */
public class LootConversionManager<L, V> {

    private final @NotNull TypeToken<V> baseType;
    private final @NotNull String keyLocation;
    private final @NotNull List<ConditionalLootConverter<L, V>> initialConverters;
    private final @NotNull List<KeyedLootConverter<L, ? extends V>> keyedConverters;
    private final @NotNull Map<String, KeyedLootConverter<L, ? extends V>> directKeyRegistry;
    private final @NotNull Map<TypeToken<? extends V>, KeyedLootConverter<L, ? extends V>> typeTokenRegistry;

    private LootConversionManager(@NotNull Builder<L, V> builder) {
        this.baseType = builder.baseType;
        this.keyLocation = builder.keyLocation;
        this.keyedConverters = List.copyOf(builder.keyedConverters);

        Map<String, KeyedLootConverter<L, ? extends V>> directKeys = new HashMap<>();
        Map<TypeToken<? extends V>, KeyedLootConverter<L, ? extends V>> typeTokens = new HashMap<>();
        for (var converter : builder.keyedConverters) {
            if (!GenericTypeReflector.isSuperType(baseType.getType(), converter.convertedType().getType())) {
                throw new IllegalArgumentException("Cannot register value with type '" + converter.convertedType().getType() + "' as it is not a subtype of '" + baseType.getType() + "'");
            }
            if (directKeys.put(converter.key(), converter) != null) {
                throw new IllegalArgumentException("Cannot register value with key '" + converter.key() + " as something with that key has already been registered");
            }
            if (typeTokens.put(converter.convertedType(), converter) != null) {
                throw new IllegalArgumentException("Cannot register value with type '" + converter.convertedType().getType() + "' as something with that type has already been registered");
            }
        }

        this.directKeyRegistry = Map.copyOf(directKeys);
        this.typeTokenRegistry = Map.copyOf(typeTokens);
        this.initialConverters = List.copyOf(builder.initialConverters);
    }

    /**
     * This is the base type of this manager. It's the input type for serializers and the output type for deserializers.
     * For {@link ConditionalLootConverter}s, it's the exact parameter for both, but subtypes of it are allowed (due to
     * polymorphism) for the actual values of them. For {@link KeyedLootConverter}s, the exact parameter type must be a
     * subtype of this base type.
     * @return the base type that all conditional converters must handle and that all keyed converters must handle a
     *         subtype of
     */
    public @NotNull TypeToken<V> baseType() {
        return baseType;
    }

    /**
     * This is the location of keys for {@link KeyedLootConverter}s. Essentially, if none of the initial converters
     * apply to an input that should be serialized (tested via
     * {@link ConditionalLootConverter#canSerialize(Object, LootConversionContext)}), a node is created and the key
     * of this keyLocation is set to the value of {@link KeyedLootConverter#key()}, assuming that a valid keyed loot
     * converter could be found.
     * @return the key location that will be added to map-typed configuration nodes
     */
    public @NotNull String keyLocation() {
        return keyLocation;
    }

    /**
     * This is the list of conditional loot converters that will be individually tested before any keyed loot converters
     * are attempted. They are tested in the order they are added when the builder is being created.
     * @return the list of initially applied converters
     */
    public @NotNull List<ConditionalLootConverter<L, V>> initialConverters() {
        return initialConverters;
    }

    /**
     * This is the list of normal converters that will be consolidated into maps based on their
     * {@link KeyedLootConverter#key() keys} and {@link KeyedLootConverter#convertedType() converted types}. These
     * should be easier to create than conditional loot converters, but at the cost of being less customizable and less
     * direct.
     * @return the list of keyed loot converters
     */
    public @NotNull List<KeyedLootConverter<L, ? extends V>> keyedConverters() {
        return keyedConverters;
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
     * @return the serialized version of the provided input
     * @param <R> the real type of {@code input}
     * @throws ConfigurateException if the input could not be serialized for some reason
     */
    public <R extends V> @NotNull ConfigurationNode serialize(@NotNull R input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        if (!initialConverters.isEmpty()) {
            for (var conditional : initialConverters) {
                if (conditional.canSerialize(input, context)) {
                    return conditional.serialize(input, context);
                }
            }
        }
        @SuppressWarnings("unchecked")
        KeyedLootConverter<L, R> converter = (KeyedLootConverter<L, R>) this.typeTokenRegistry.get(TypeToken.get(input.getClass()));
        if (converter == null) {
            throw new ConfigurateException("Could not find a valid keyed loot converter for type '" + input.getClass() + "'");
        }
        ConfigurationNode node = context.loader().createNode();
        node.node(this.keyLocation).set(converter.key());
        converter.serialize(input, node, context);
        return node;
    }

    /**
     * Deserializes the provided configuration node into an instance of (or an instance of a subtype of) {@link V}.<br>
     * As it may not be self-explanatory, here's specifically how the process works:<br>
     * All of the initial converters (that were added to the builder) are each checked, in the order they were added to
     * the aforementioned border, to see if they will deserialize the provided input. If any one of them does, it is
     * used to deserialize the input and the result is returned. These basically function as an extremely customizable
     * alternative to keyed loot converters.<br>
     * Otherwise, the input's child at the {@link #keyLocation()} key is used to determine which keyed converter to use,
     * and then the converter is used to deserialize the input.  If this process couldn't be done for any reason, an
     * exception explaining why is thrown.
     * @param input the configuration node to deserialize into a {@link V}
     * @param context the context object, to use if required
     * @return the deserialized version of the provided input
     * @throws ConfigurateException if the input could not be deserialized for some reason
     */
    public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        if (!initialConverters.isEmpty()) {
            for (var conditional : initialConverters) {
                if (conditional.canDeserialize(input, context)) {
                    return conditional.deserialize(input, context);
                }
            }
        }
        ConfigurationNode keyNode = input.node(this.keyLocation);
        String actualKey = Utils.require(keyNode, String.class);
        KeyedLootConverter<L, ? extends V> converter = this.directKeyRegistry.get(actualKey);
        if (converter == null) {
            throw new ConfigurateException(keyNode, "Could not find a valid keyed loot converter for key '" + actualKey + "'");
        }
        return converter.deserialize(input, context);
    }

    /**
     * Creates a new builder for this class, with empty lists of converters and all other fields as null.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new LootConversionManager builder
     * @param <L> the loot item type
     * @param <V> the base type of converted objects
     */
    @Contract(" -> new")
    public static <L, V> @NotNull Builder<L, V> builder() {
        return new Builder<>();
    }

    public static final class Builder<L, V> {

        private TypeToken<V> baseType;
        private String keyLocation;
        private final @NotNull List<KeyedLootConverter<L, ? extends V>> keyedConverters = new ArrayList<>();
        private final @NotNull List<ConditionalLootConverter<L, V>> initialConverters = new ArrayList<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder<L, V> baseType(@NotNull TypeToken<V> baseType) {
            this.baseType = baseType;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L, V> keyLocation(@NotNull String keyLocation) {
            this.keyLocation = keyLocation;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L, V> addConverter(@NotNull KeyedLootConverter<L, ? extends V> converter) {
            this.keyedConverters.add(converter);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L, V> addInitialConverter(@NotNull ConditionalLootConverter<L, V> converter) {
            this.initialConverters.add(converter);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootConversionManager<L, V> build() {
            Objects.requireNonNull(baseType, "LootConversionManager instances cannot be built without a base type!");
            Objects.requireNonNull(keyLocation, "LootConversionManager instances cannot be built without a key location!");
            return new LootConversionManager<>(this);
        }
    }

}
