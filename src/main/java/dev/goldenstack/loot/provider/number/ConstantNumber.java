package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonSerializationManager;
import dev.goldenstack.loot.json.LootDeserializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Represents a {@code NumberProvider} that is a constant value.
 */
public record ConstantNumber(double value) implements NumberProvider {
    /**
     * The immutable key for all {@code ConstantNumber}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "constant");

    /**
     * {@inheritDoc}<br>
     * For {@code ConstantNumber}s, the value should never change.
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.addProperty("value", this.value);
    }

    /**
     * {@inheritDoc}
     * @return {@link #KEY}
     */
    @Override
    public @NotNull NamespaceID getKey() {
        return KEY;
    }

    @Override
    public @NotNull LootDeserializer<NumberProvider> getDeserializer() {
        return ConstantNumber::deserialize;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code ConstantNumber}
     */
    public static @NotNull NumberProvider deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new ConstantNumber(JsonHelper.assureNumber(json.get("value"), "value").doubleValue());
    }

    /**
     * A method to use as a method reference for {@link JsonSerializationManager#defaultDeserializer(BiFunction)} when the
     * number provider manager needs the default deserializer.
     */
    public static @Nullable NumberProvider defaultDeserializer(@Nullable JsonElement element, @NotNull JsonSerializationManager<NumberProvider> manager) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        return new ConstantNumber(element.getAsNumber().doubleValue());
    }
}