package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that wraps another condition and inverts the result.
 */
public record InvertedCondition(@NotNull LootCondition condition) implements LootCondition {
    /**
     * The immutable key for all {@code InvertedCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "inverted");

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        object.add("term", loader.getLootConditionManager().serialize(this.condition));
    }

    /**
     * {@inheritDoc}
     * @return {@link #KEY}
     */
    @Override
    public @NotNull NamespaceID getKey() {
        return KEY;
    }

    /**
     * Returns the inverted result of this instance's {@link #condition}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return !condition.test(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return InvertedCondition::deserialize;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code InvertedCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        return new InvertedCondition(loader.getLootConditionManager().deserialize(json.get("term"), "term"));
    }
}