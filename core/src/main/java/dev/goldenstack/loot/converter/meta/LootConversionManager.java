package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.Trove;
import dev.goldenstack.loot.converter.ConditionalLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages serialization for when multiple subtypes of a base class must be chosen from.
 * @param <V> the base type of object that will be converted
 */
public class LootConversionManager<V> implements TypedLootConverter<V> {

    private final @NotNull TypeToken<V> baseType;
    private final @NotNull String keyLocation;
    private final @NotNull List<ConditionalLootConverter<V>> initialConverters;
    private final @NotNull Map<String, TypedLootConverter<? extends V>> directKeyRegistry;
    private final @NotNull Map<TypeToken<? extends V>, TypedLootConverter<? extends V>> typeTokenRegistry;
    private final @NotNull Map<TypeToken<? extends V>, String> reverseKeyRegistry;

    private LootConversionManager(@NotNull Builder<V> builder) {
        this.baseType = Objects.requireNonNull(builder.baseType, "This builder cannot be built without a base type");
        this.keyLocation = Objects.requireNonNull(builder.keyLocation, "This builder cannot be built without a key location");

        this.directKeyRegistry = Map.copyOf(builder.typedConverters);
        this.typeTokenRegistry = builder.typedConverters.values().stream().collect(Collectors.toMap(TypedLootConverter::convertedType, Function.identity()));
        this.reverseKeyRegistry = builder.typedConverters.entrySet().stream().collect(Collectors.toMap(a -> a.getValue().convertedType(), Map.Entry::getKey));

        this.initialConverters = List.copyOf(builder.initialConverters);
    }

    /**
     * This is the base type of this manager. It's the input type for serializers and the output type for deserializers.
     * For {@link ConditionalLootConverter}s, it's the exact parameter for both, but subtypes of it are allowed (due to
     * polymorphism) for the actual values of them. For {@link TypedLootConverter}s, the exact parameter type must be a
     * subtype of this base type.
     * @return the base type that all conditional converters must handle and that all typed converters must handle a
     *         subtype of
     */
    @Override
    public @NotNull TypeToken<V> convertedType() {
        return baseType;
    }

    /**
     * Serializes the provided object into a configuration node.<br>
     * As it may not be self-explanatory, here's specifically how the process works:<br>
     * All of the initial converters (that were added to the builder) are each checked, in the order they were added to
     * the aforementioned border, to see if they will serialize the provided input. If any one of them does, it is used
     * to serialize the input and the result is returned. These basically function as an extremely customizable
     * alternative to typed loot converters.<br>
     * Otherwise, this manager looks for the typed converter that has a {@link TypedLootConverter#convertedType()} equal
     * to the type of the input and uses that to serialize it. If this process couldn't be done for any reason, an
     * exception explaining why is thrown.
     * @param input the object to serialize into a configuration node
     * @param context the context object, to use if required
     * @throws ConfigurateException if the input could not be serialized for some reason
     */
    public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
        serialize0(input, result, context);
    }

    private <R extends V> void serialize0(@NotNull R input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
        if (!initialConverters.isEmpty()) {
            for (var conditional : initialConverters) {
                if (conditional.canSerialize(input, context)) {
                    conditional.serialize(input, result, context);
                    return;
                }
            }
        }
        TypeToken<?> token = TypeToken.get(input.getClass());

        @SuppressWarnings("unchecked")
        TypedLootConverter<R> converter = (TypedLootConverter<R>) typeTokenRegistry.get(token);
        String key = reverseKeyRegistry.get(token);
        if (converter == null || key == null) {
            throw new ConfigurateException("Unknown input type '" + input.getClass() + "' for base type '" + baseType.getType() + "'");
        }
        result.node(keyLocation).set(key);
        converter.serialize(input, result, context);
    }

    /**
     * Deserializes the provided configuration node into an instance of (or an instance of a subtype of) {@link V}.<br>
     * As it may not be self-explanatory, here's specifically how the process works:<br>
     * All of the initial converters (that were added to the builder) are each checked, in the order they were added to
     * the aforementioned border, to see if they will deserialize the provided input. If any one of them does, it is
     * used to deserialize the input and the result is returned. These basically function as an extremely customizable
     * alternative to typed loot converters.<br>
     * Otherwise, the input's child at the key location is used to determine which typed converter to use, and then the
     * converter is used to deserialize the input. If this process couldn't be done for any reason, an exception
     * explaining why is thrown.
     * @param input the configuration node to deserialize into a {@link V}
     * @param context the context object, to use if required
     * @return the deserialized version of the provided input
     * @throws ConfigurateException if the input could not be deserialized for some reason
     */
    @Override
    public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull Trove context) throws ConfigurateException {
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

        TypedLootConverter<? extends V> converter = directKeyRegistry.get(actualKey);
        if (converter == null) {
            throw new ConfigurateException(keyNode, "Unknown key '" + actualKey + "' for base type '" + convertedType().getType() + "'");
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
        private final @NotNull Map<String, TypedLootConverter<? extends V>> typedConverters = new HashMap<>();
        private final @NotNull List<ConditionalLootConverter<V>> initialConverters = new ArrayList<>();

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

        @Contract("_, _ -> this")
        public @NotNull Builder<V> addConverter(@NotNull String key, @NotNull TypedLootConverter<? extends V> converter) {
            this.typedConverters.put(key, converter);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<V> addInitialConverter(@NotNull ConditionalLootConverter<V> converter) {
            this.initialConverters.add(converter);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootConversionManager<V> build() {
            return new LootConversionManager<>(this);
        }
    }

}
