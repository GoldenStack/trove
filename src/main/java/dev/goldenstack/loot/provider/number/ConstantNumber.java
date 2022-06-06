package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
import dev.goldenstack.loot.json.JsonSerializationManager;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Represents a {@code NumberProvider} that is a constant value.
 */
public record ConstantNumber(double value) implements NumberProvider {
    /**
     * {@inheritDoc}<br>
     * For {@code ConstantNumber}s, the value should never change.
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        return value;
    }

    public static final @NotNull BiFunction<JsonElement, JsonSerializationManager<? extends NumberProvider>, NumberProvider> DEFAULT_DESERIALIZER = (element, jsonSerializationManager) -> {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        return new ConstantNumber(element.getAsNumber().doubleValue());
    };

    public static final @NotNull JsonLootConverter<ConstantNumber> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:constant"), ConstantNumber.class) {
        @Override
        public @NotNull ConstantNumber deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new ConstantNumber(JsonHelper.assureNumber(json.get("value"), "value").doubleValue());
        }

        @Override
        public void serialize(@NotNull ConstantNumber input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.addProperty("value", input.value);
        }
    };
}