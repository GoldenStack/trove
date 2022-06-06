package dev.goldenstack.loot.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Manages serialization and deserialization for groups of serializable classes.
 */
public class JsonSerializationManager <T> {

    private final @NotNull ImmuTables owner;
    private final @NotNull String elementName;

    private final @NotNull Object lock;
    private final @NotNull Map<NamespaceID, JsonLootConverter<? extends T>> keyRegistry;
    private final @NotNull Map<Class<? extends T>, JsonLootConverter<? extends T>> classRegistry;

    private @Nullable BiFunction<JsonElement, JsonSerializationManager<? extends T>, T> defaultDeserializer;

    public JsonSerializationManager(@NotNull ImmuTables owner, @NotNull String elementName) {
        this.owner = owner;
        this.elementName = elementName;

        this.lock = new Object();
        this.keyRegistry = new HashMap<>();
        this.classRegistry = new HashMap<>();
    }

    /**
     * Returns this JsonSerializationManager's default deserializer
     */
    public @Nullable BiFunction<JsonElement, JsonSerializationManager<? extends T>, T> defaultDeserializer() {
        return defaultDeserializer;
    }

    /**
     * Sets this JsonSerializationManager's default deserializer to the provided value
     */
    public void defaultDeserializer(@Nullable BiFunction<JsonElement, JsonSerializationManager<? extends T>, T> defaultDeserializer) {
        this.defaultDeserializer = defaultDeserializer;
    }

    /**
     * Adds the provided value to this manager by its {@link JsonLootConverter#key()}.<br>
     * Throws an {@code IllegalArgumentException} if there is already another registered converter with its key or class.
     */
    public void register(@NotNull JsonLootConverter<? extends T> value) {
        synchronized (lock) {
            if (this.keyRegistry.containsKey(value.key())) {
                throw new IllegalArgumentException("Cannot register value for key '" + value.key() + "' as something with that key is already registered");
            }
            if (this.classRegistry.containsKey(value.builtClass())) {
                throw new IllegalArgumentException("Cannot register value for class '" + value.builtClass() + "' as something with that class is already registered");
            }
            this.keyRegistry.put(value.key(), value);
            this.classRegistry.put(value.builtClass(), value);
        }
    }

    /**
     * Unregisters any converters in this manager that have the provided key.<br>
     * Throws an {@code IllegalArgumentException} if there is not a registered converter with the provided key
     */
    public void unregister(@NotNull NamespaceID key) {
        synchronized (lock) {
            var result = this.keyRegistry.get(key);
            if (result == null) {
                throw new IllegalArgumentException("Cannot unregister value for key '" + key + "' because there is nothing registered under it");
            }
            this.keyRegistry.remove(key);
            this.classRegistry.remove(result.builtClass(), result);
        }
    }

    /**
     * Attempts to find a {@link JsonLootConverter} based on the provided NamespaceID
     * @param key The key to search for
     * @return The converter that was found, or null if none was found.
     */
    public @Nullable JsonLootConverter<? extends T> request(@NotNull NamespaceID key) {
        synchronized (lock) {
            return this.keyRegistry.get(key);
        }
    }

    /**
     * Attempts to find a {@link JsonLootConverter} based on the provided class
     * @param possibleClass The class to search for
     * @return The converter that was found, or null if none was found.
     */
    @SuppressWarnings("unchecked")
    public <U extends T> @Nullable JsonLootConverter<U> request(@NotNull Class<?> possibleClass) {
        synchronized (lock) {
            return (JsonLootConverter<U>) this.classRegistry.get(possibleClass);
        }
    }

    /**
     * Clears all of the registered values from this JsonSerializationManager
     */
    public void clear() {
        synchronized (lock) {
            this.keyRegistry.clear();
            this.classRegistry.clear();
        }
    }

    /**
     * @return This manager's {@code elementName} that it uses for serialization and deserialization.
     */
    public @NotNull String getElementName() {
        return elementName;
    }

    /**
     * Serializes the provided object according to its converter. Technically, a JsonSerializationManager isn't
     * needed for this, but it makes it simpler and automatically manages things such as the element name.
     * @param t The object to serialize
     * @return The generated JsonObject
     * @throws JsonParseException if the argument's deserializer throws an error
     */
    public <U extends T> @NotNull JsonObject serialize(@NotNull U t) throws JsonParseException {
        JsonObject object = new JsonObject();
        JsonLootConverter<U> converter = request(t.getClass());
        if (converter == null) {
            throw new JsonParseException("Can not find JsonLootConverter for class '" + t.getClass() + "'");
        }
        object.addProperty(this.elementName, converter.key().asString());
        converter.serialize(t, object, this.owner);
        return object;
    }

    /**
     * Deserializes the provided JsonElement. If this has a {@link #defaultDeserializer} and the element could not
     * be deserialized (not from the deserializer, but, for example, if the element wasn't a JsonElement), the default
     * deserializer will be given the element to parse. If it returns null or there is no default deserializer, an error
     * will be thrown.
     * @param element The JsonElement that should be deserialized
     * @param key The key (to improve error messages). Setting it to null will just change the message slightly.
     * @return The deserialized object
     * @throws JsonParseException if there is an error in the deserialization process
     */
    public @NotNull T deserialize(@Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        if (element != null && element.isJsonObject()){
            JsonObject object = element.getAsJsonObject();
            String type = JsonHelper.getAsString(object.get(elementName));
            if (type != null){
                JsonLootConverter<? extends T> t = this.keyRegistry.get(NamespaceID.from(type));
                if (t != null) {
                    return t.deserialize(object, this.owner);
                }
                throw new JsonParseException("Could not find deserializer for type \"" + type + "\"!");
            }
        }
        if (this.defaultDeserializer != null) {
            T t = this.defaultDeserializer.apply(element, this);
            if (t != null) {
                return t;
            }
        }
        throw new JsonParseException(JsonHelper.expectedNotNullMessage(key, element));
    }
}