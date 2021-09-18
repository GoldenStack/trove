package dev.goldenstack.loot.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public interface LootJsonDeserializable <T extends LootJsonSerializable<?>> {
    @NotNull T deserialize(@NotNull JsonObject json) throws JsonParseException;
    @NotNull NamespaceID getKey();
}
