package dev.goldenstack.loot.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.ImmuTables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Manages serialization and deserialization ("conversion") for groups of classes that all stem from the same source.
 * @param <L> the loot item
 * @param <T> the base class that will be serialized and deserialized
 */
public class LootConversionManager<L, T extends LootAware<L>> {

    private final @NotNull ImmuTables<L> owner;
    private final @NotNull String keyLocation;
    private final @Nullable BiFunction<JsonElement, ImmuTables<L>, T> fallbackDeserializer;

    private final @NotNull Map<String, LootConverter<L, ? extends T>> keyRegistry;
    private final @NotNull Map<Class<? extends T>, LootConverter<L, ? extends T>> classRegistry;

    private LootConversionManager(@NotNull Builder<L, T> builder) {
        this.owner = builder.owner;
        this.keyLocation = builder.keyLocation;
        this.fallbackDeserializer = builder.fallbackDeserializer;
        this.keyRegistry = Map.copyOf(builder.keyRegistry);
        this.classRegistry = Map.copyOf(builder.classRegistry);
    }

    /**
     * @return the owner of this manager
     */
    public @NotNull ImmuTables<L> owner() {
        return owner;
    }

    /**
     * @return the location in JSON objects at which keys will be searched for
     */
    public @NotNull String keyLocation() {
        return keyLocation;
    }

    /**
     * @return the deserializer that is used if, at some point, a JSON element that isn't a JSON object needs to be deserialized
     */
    public @Nullable BiFunction<JsonElement, ImmuTables<L>, T> fallbackDeserializer() {
        return fallbackDeserializer;
    }

    /**
     * @return the converter that is associated with the provided key
     */
    public @Nullable LootConverter<L, ? extends T> request(@NotNull String key) {
        return keyRegistry.get(key);
    }

    /**
     * Serializes the provided object into a JsonObject.
     * @param n the object to serialize
     * @return a JsonObject representing the serialized state of the provided object
     * @param <N> the type of the object that will be serialized
     * @throws LootParsingException if something happens during serialization or if a valid loot converter couldn't be found
     */
    public <N extends T> @NotNull JsonObject serialize(@NotNull N n) throws LootParsingException {
        JsonObject object = new JsonObject();
        //noinspection unchecked - We, at this point, know that it must be a LootConverter<L, N>.
        LootConverter<L, N> converter = (LootConverter<L, N>) this.classRegistry.get(n.getClass());
        if (converter == null) {
            throw new LootParsingException("Could not find a LootConverter for class '" + n.getClass() + "'");
        }
        object.addProperty(this.keyLocation, converter.key());
        converter.serialize(n, object, this.owner);
        return object;
    }

    /**
     * Deserializes the provided element into an instance of something that extends {@link T}.
     * @param element the element to deserialize
     * @return the instance of something extending {@code T} that was deserialized
     */
    public @NotNull T deserialize(@Nullable JsonElement element) throws LootParsingException {
        if (element != null && element.isJsonObject()){
            JsonObject object = element.getAsJsonObject();
            JsonElement rawType = object.get(keyLocation);
            if (rawType != null && rawType.isJsonPrimitive()){
                String type = rawType.getAsString();
                LootConverter<L, ? extends T> t = this.keyRegistry.get(type);
                if (t != null) {
                    return t.deserialize(object, this.owner);
                }
                throw new LootParsingException("Could not find deserializer for type \"" + type + "\"!");
            }
        }
        if (this.fallbackDeserializer != null) {
            T t = this.fallbackDeserializer.apply(element, this.owner);
            if (t != null) {
                return t;
            }
        }
        throw new LootParsingException("Expected the provided element to not be null!");
    }

    /**
     * @return a new builder for manager instances
     */
    @Contract(" -> new")
    public static <L, T extends LootAware<L>> @NotNull Builder<L, T> builder() {
        return new Builder<>();
    }

    public static final class Builder<L, T extends LootAware<L>> {

        private ImmuTables<L> owner;
        private String keyLocation;
        private BiFunction<JsonElement, ImmuTables<L>, T> fallbackDeserializer;
        private final @NotNull Map<String, LootConverter<L, ? extends T>> keyRegistry = new HashMap<>();
        private final @NotNull Map<Class<? extends T>, LootConverter<L, ? extends T>> classRegistry = new HashMap<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder<L, T> owner(@NotNull ImmuTables<L> owner) {
            this.owner = owner;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L, T> keyLocation(@NotNull String keyLocation) {
            this.keyLocation = keyLocation;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L, T> fallbackDeserializer(@NotNull BiFunction<JsonElement, ImmuTables<L>, T> fallbackDeserializer) {
            this.fallbackDeserializer = fallbackDeserializer;
            return this;
        }

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

        @Contract(" -> new")
        public @NotNull LootConversionManager<L, T> build() {
            Objects.requireNonNull(owner, "LootConversionManager instances cannot be built without an owner!");
            Objects.requireNonNull(keyLocation, "LootConversionManager instances cannot be built without an owner!");
            return new LootConversionManager<>(this);
        }
    }

}
