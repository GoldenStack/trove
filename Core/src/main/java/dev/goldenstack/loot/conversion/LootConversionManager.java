package dev.goldenstack.loot.conversion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.util.JsonUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages serialization and deserialization ("conversion") for groups of classes that all stem from the same source.
 * @param <L> the loot item
 * @param <T> the base class that will be serialized and deserialized
 */
public class LootConversionManager<L, T extends LootAware<L>> {

    private final @NotNull String keyLocation;

    private final @NotNull Map<String, LootConverter<L, ? extends T>> keyRegistry;
    private final @NotNull Map<Class<? extends T>, LootConverter<L, ? extends T>> classRegistry;

    private final @NotNull List<ComplexLootConverter<L, T>> complexConverters;

    private LootConversionManager(@NotNull Builder<L, T> builder) {
        this.keyLocation = builder.keyLocation;
        this.keyRegistry = Map.copyOf(builder.keyRegistry);
        this.classRegistry = Map.copyOf(builder.classRegistry);
        this.complexConverters = List.copyOf(builder.complexConverters);
    }

    /**
     * @return the location in JSON objects at which keys will be searched for
     */
    public @NotNull String keyLocation() {
        return keyLocation;
    }

    /**
     * @param key the key to get from this manager's internal map
     * @return the converter that is associated with the provided key
     */
    public @Nullable LootConverter<L, ? extends T> request(@NotNull String key) {
        return keyRegistry.get(key);
    }

    /**
     * Serializes the provided object into a JSON element.
     * @param s the object to serialize
     * @param context the context to feed to converters for serialization
     * @return a JSON element representing the serialized state of the provided object
     * @param <S> the type of the object that will be serialized
     * @throws LootConversionException if something happens during serialization or if a valid loot converter couldn't be found
     */
    public <S extends T> @NotNull JsonElement serialize(@NotNull S s, @NotNull LootConversionContext<L> context) throws LootConversionException {
        if (!complexConverters.isEmpty()) {
            for (ComplexLootConverter<L, T> complexConverter : complexConverters) {
                if (complexConverter.canSerialize(s, context)) {
                    return complexConverter.serialize(s, context);
                }
            }
        }
        @SuppressWarnings("unchecked") // We, at this point, know that it must be a LootConverter<L, S>.
        LootConverter<L, S> converter = (LootConverter<L, S>) this.classRegistry.get(s.getClass());
        if (converter == null) {
            throw new LootConversionException("Could not find a LootConverter for class '" + s.getClass() + "'");
        }
        JsonObject object = new JsonObject();
        object.addProperty(this.keyLocation, converter.key());
        converter.serialize(s, object, context);
        return object;
    }

    /**
     * Deserializes the provided element into an instance of something that extends {@link T}.
     * @param element the element to deserialize
     * @param context the context to feed to converters for deserialization
     * @return the instance of something extending {@code T} that was deserialized
     * @throws LootConversionException if something happens during deserialization or if a valid key couldn't be found in the element
     */
    public @NotNull T deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<L> context) throws LootConversionException {
        if (!complexConverters.isEmpty()) {
            for (ComplexLootConverter<L, T> complexConverter : complexConverters) {
                if (complexConverter.canDeserialize(element, context)) {
                    return complexConverter.deserialize(element, context);
                }
            }
        }
        JsonObject object = JsonUtils.assureJsonObject(element, null);
        String type = JsonUtils.assureString(object.get(keyLocation), keyLocation);
        LootConverter<L, ? extends T> t = this.keyRegistry.get(type);
        if (t == null) {
            throw new LootConversionException("Could not find deserializer for type \"" + type + "\"!");
        }
        return t.deserialize(object, context);
    }

    /**
     * Utility method to serialize a list of {@link T}
     * @param list the list of {@link T} to attempt to serialize
     * @param context the context to feed into {@link #serialize(LootAware, LootConversionContext)} when it is called on
     *                each element
     * @return the complete JsonArray of serialized elements
     * @throws LootConversionException if one of the elements could not be serialized
     */
    public @NotNull JsonArray serializeList(@NotNull List<T> list, @NotNull LootConversionContext<L> context) throws LootConversionException {
        return JsonUtils.serializeJsonArray(list, a -> this.serialize(a, context));
    }

    /**
     * Utility method to deserialize a list of {@link T}
     * @param array the JsonArray to attempt to deserialize
     * @param context the context to feed into {@link #deserialize(JsonElement, LootConversionContext)} when it is
     *                called on each element
     * @return the complete list of deserialized elements
     * @throws LootConversionException if one of the elements could not be deserialized
     */
    public @NotNull List<T> deserializeList(@NotNull JsonArray array, @NotNull LootConversionContext<L> context) throws LootConversionException {
        return JsonUtils.deserializeJsonArray(array, null, (a, b) -> this.deserialize(a, context));
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

        private String keyLocation;
        private final @NotNull Map<String, LootConverter<L, ? extends T>> keyRegistry = new HashMap<>();
        private final @NotNull Map<Class<? extends T>, LootConverter<L, ? extends T>> classRegistry = new HashMap<>();
        private final @NotNull List<ComplexLootConverter<L, T>> complexConverters = new ArrayList<>();

        private Builder() {}

        /**
         * @param keyLocation the new location of the {@link LootConverter#key()} for managers built with this builder
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
        public @NotNull Builder<L, T> addConverter(@NotNull LootConverter<L, ? extends T> converter) {
            if (this.keyRegistry.containsKey(converter.key())) {
                throw new IllegalArgumentException("Cannot register value for key '" + converter.key() + "' as something with that key is already registered");
            }
            if (this.classRegistry.containsKey(converter.convertedClass())) {
                throw new IllegalArgumentException("Cannot register value for class '" + converter.convertedClass() + "' as something with that class is already registered");
            }
            this.keyRegistry.put(converter.key(), converter);
            this.classRegistry.put(converter.convertedClass(), converter);
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
            Objects.requireNonNull(keyLocation, "LootConversionManager instances cannot be built without a key location!");
            return new LootConversionManager<>(this);
        }
    }

}
