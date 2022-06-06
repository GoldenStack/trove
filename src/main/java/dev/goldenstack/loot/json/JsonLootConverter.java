package dev.goldenstack.loot.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Handles serialization and deserialization for whatever {@code <T>} is. This implementation stores the {@link Class}
 * object to assure, during runtime, that everything is correct, without having to trust the object instance itself for
 * its information.
 */
public abstract class JsonLootConverter<T> {

    private final @NotNull NamespaceID key;
    private final @NotNull Class<T> builtClass;

    public JsonLootConverter(@NotNull NamespaceID key, @NotNull Class<T> builtClass) {
        this.key = key;
        this.builtClass = builtClass;
    }

    /**
     * @return the NamespaceID that this converter represents
     */
    public @NotNull NamespaceID key() {
        return key;
    }

    /**
     * @return the class of the object that is being serialized and deserialized
     */
    public @NotNull Class<T> builtClass() {
        return builtClass;
    }

    /**
     * Deserializes the provided JsonObject into an instance of {@code T}.
     * @param json the object to deserialize
     * @param loader the loader, for other required deserialization
     * @return the instance of {@code T} that was created from the JsonObject
     * @throws JsonParseException if something goes wrong while deserializing
     */
    public abstract @NotNull T deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException;

    /**
     * Serializes the provided {@code T} instance onto the JsonObject. The {@code result} will usually already have one
     * field set (the key), but it's not a good idea to rely on the current state of it.
     * @param input the object to serialize
     * @param result the JsonObject that should have fields added to it
     * @param loader the loader, for other required deserialization
     * @throws JsonParseException if something goes wrong while serializing
     */
    public abstract void serialize(@NotNull T input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException;

}
