package dev.goldenstack.loot.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;

public interface LootJsonSerializable <T> {
    void serialize(@NotNull JsonObject object) throws JsonParseException;

    @NotNull LootJsonDeserializable<? extends LootJsonSerializable<T>> getDeserializer();
}
