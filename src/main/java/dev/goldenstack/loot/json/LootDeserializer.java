package dev.goldenstack.loot.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a deserializer that can deserialize a specific class that extends {@link LootSerializer}.
 * <p>
 *     <b>Please do not extend this class.</b> Instead, use a method reference.
 * </p>
 * @param <T> Something that is {@link LootSerializer}
 */
@FunctionalInterface
public interface LootDeserializer<T extends LootSerializer<?>> {

    /**
     * Deserializes the provided JsonObject. If this instance has been registered to a {@link JsonSerializationManager}
     * and the manager is calling this method, it is safe to assume that the object has a value associated with the key
     * equal to the manager's {@link JsonSerializationManager#getElementName() elementName}, although I still do
     * recommend checking it yourself if you'd like.
     * @param json The JsonObject to be deserialized
     * @param loader The LootTableLoader, to make it easier to deserialize things accurately.
     * @return The deserialized object
     * @throws JsonParseException if any errors occur
     */
    @NotNull T deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException;
}
