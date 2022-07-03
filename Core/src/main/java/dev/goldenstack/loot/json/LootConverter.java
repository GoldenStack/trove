package dev.goldenstack.loot.json;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.ImmuTables;
import org.jetbrains.annotations.NotNull;

/**
 * Handles serialization and deserialization ("conversion") for whatever {@code <T>} is. This implementation stores the
 * {@link Class} object in order for any managers this is registered in to detect which converter to use without relying
 * on the object itself to provide it, theoretically allowing for objects that do not have a converter to be used in
 * loot tables, although this is generally not a good idea.
 * @param <L> the loot item
 * @param <T> the class that can be converted
 */
public abstract class LootConverter<L, T extends LootAware<L>> {

    private final @NotNull String key;
    private final @NotNull Class<T> convertedClass;

    public LootConverter(@NotNull String key, @NotNull Class<T> convertedClass) {
        this.key = key;
        this.convertedClass = convertedClass;
    }

    /**
     * @return the key of this converter
     */
    public final @NotNull String key() {
        return key;
    }

    /**
     * @return the class of the objects that are theoretically converted
     */
    public final Class<T> convertedClass() {
        return convertedClass;
    }

    /**
     * Deserializes the provided JSON object into an instance of {@code T}. Although the JSON object is mutable, it's
     * not a good idea to modify fields on it without knowing specifically where its source is.
     * @param json the JSON object that should be deserialized
     * @param loader the loader, to use for any other required deserialization
     * @return the instance of {@code T} that was deserialized
     * @throws LootParsingException if, for some reason, something goes wrong while deserializing
     */
    public abstract @NotNull T deserialize(@NotNull JsonObject json, @NotNull ImmuTables<L> loader) throws LootParsingException;

    /**
     * Serializes the provided instance of {@code T} onto the provided JSON object. The JSON object will usually already
     * have one field set (the key, which is probably used for deserialization), so overwriting that key should be
     * avoided. However, it's likely that the specific location of the key will be known, so it shouldn't be difficult
     * to avoid overwriting it. Additionally, it's not a good idea to rely on the state of the JSON object anyway.
     * @param input the input object that will be serialized
     * @param result the JSON object that should have fields added to it when serializing
     * @param loader the loader, to use for any other required serialization
     * @throws LootParsingException if, for some reason, something goes wrong while serializing
     */
    public abstract void serialize(@NotNull T input, @NotNull JsonObject result, @NotNull ImmuTables<L> loader) throws LootParsingException;
}
