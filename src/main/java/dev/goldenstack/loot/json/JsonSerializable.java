package dev.goldenstack.loot.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something that can be serialized to a JsonObject. The method adds data to the provided JsonObject instead
 * of creating its own, to allow for extra data to be added before the serialization part.
 * <p>Must be paired with a {@link JsonDeserializable}</p>
 */
public interface JsonSerializable<T> {
    /**
     * Adds data to the provided JsonObject. When using this method, do not create your own JsonObject. The reason this
     * method adds data to a JsonObject instead of creating its own is to allow whatever is serializing this object to
     * add data to it before the serialization method does.
     * @param object The object to add data to
     * @throws JsonParseException if any errors occur
     */
    void serialize(@NotNull JsonObject object) throws JsonParseException;

    /**
     * @return This LootJsonSerializable's deserializer. Typically, returning {@code null} will not break the code
     * unless it has been added to a {@link JsonSerializationManager} and somewhere in the code the method
     * {@link JsonSerializationManager#serialize(JsonSerializable)} is run.
     */
    @NotNull JsonDeserializable<? extends JsonSerializable<T>> getDeserializer();
}
