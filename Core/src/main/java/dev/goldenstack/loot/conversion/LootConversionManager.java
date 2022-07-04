package dev.goldenstack.loot.conversion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.ImmuTables;
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

    private final @NotNull ImmuTables<L> owner;
    private final @NotNull String keyLocation;

    private final @NotNull Map<String, LootConverter<L, ? extends T>> keyRegistry;
    private final @NotNull Map<Class<? extends T>, LootConverter<L, ? extends T>> classRegistry;

    private final @NotNull List<ComplexLootConverter<L, T>> complexConverters;

    private LootConversionManager(@NotNull Builder<L, T> builder) {
        this.owner = builder.owner;
        this.keyLocation = builder.keyLocation;
        this.keyRegistry = Map.copyOf(builder.keyRegistry);
        this.classRegistry = Map.copyOf(builder.classRegistry);
        this.complexConverters = builder.complexConverters;
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
     * @return the converter that is associated with the provided key
     */
    public @Nullable LootConverter<L, ? extends T> request(@NotNull String key) {
        return keyRegistry.get(key);
    }

    /**
     * Serializes the provided object into a JSON element.
     * @param s the object to serialize
     * @return a JSON element representing the serialized state of the provided object
     * @param <S> the type of the object that will be serialized
     * @throws LootParsingException if something happens during serialization or if a valid loot converter couldn't be found
     */
    public <S extends T> @NotNull JsonElement serialize(@NotNull S s) throws LootParsingException {
        if (!complexConverters.isEmpty()) {
            for (ComplexLootConverter<L, T> complexConverter : complexConverters) {
                if (complexConverter.canSerialize(s, this)) {
                    return complexConverter.serialize(s, this);
                }
            }
        }
        @SuppressWarnings("unchecked") // We, at this point, know that it must be a LootConverter<L, S>.
        LootConverter<L, S> converter = (LootConverter<L, S>) this.classRegistry.get(s.getClass());
        if (converter == null) {
            throw new LootParsingException("Could not find a LootConverter for class '" + s.getClass() + "'");
        }
        JsonObject object = new JsonObject();
        object.addProperty(this.keyLocation, converter.key());
        converter.serialize(s, object, this.owner);
        return object;
    }

    /**
     * Deserializes the provided element into an instance of something that extends {@link T}.
     * @param element the element to deserialize
     * @return the instance of something extending {@code T} that was deserialized
     */
    public @NotNull T deserialize(@Nullable JsonElement element) throws LootParsingException {
        if (!complexConverters.isEmpty()) {
            for (ComplexLootConverter<L, T> complexConverter : complexConverters) {
                if (complexConverter.canDeserialize(element, this)) {
                    return complexConverter.deserialize(element, this);
                }
            }
        }
        JsonObject object = JsonUtils.assureJsonObject(element, null);
        String type = JsonUtils.assureString(object.get(keyLocation), keyLocation);
        LootConverter<L, ? extends T> t = this.keyRegistry.get(type);
        if (t == null) {
            throw new LootParsingException("Could not find deserializer for type \"" + type + "\"!");
        }
        return t.deserialize(object, this.owner);
    }

    /**
     * Utility method to serialize a list of {@link T}
     */
    public @NotNull JsonArray serializeList(@NotNull List<T> list) throws LootParsingException {
        return JsonUtils.serializeJsonArray(list, this::serialize);
    }

    /**
     * Utility method to deserialize a list of {@link T}
     */
    public @NotNull List<T> deserializeList(@NotNull JsonArray array) throws LootParsingException {
        return JsonUtils.deserializeJsonArray(array, null, (a, b) -> this.deserialize(a));
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
        private final @NotNull Map<String, LootConverter<L, ? extends T>> keyRegistry = new HashMap<>();
        private final @NotNull Map<Class<? extends T>, LootConverter<L, ? extends T>> classRegistry = new HashMap<>();
        private final @NotNull List<ComplexLootConverter<L, T>> complexConverters = new ArrayList<>();

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

        @Contract("_ -> this")
        public @NotNull Builder<L, T> addComplexConverter(@NotNull ComplexLootConverter<L, T> converter) {
            this.complexConverters.add(converter);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootConversionManager<L, T> build() {
            Objects.requireNonNull(owner, "LootConversionManager instances cannot be built without an owner!");
            Objects.requireNonNull(keyLocation, "LootConversionManager instances cannot be built without a key location!");
            return new LootConversionManager<>(this);
        }
    }

}
