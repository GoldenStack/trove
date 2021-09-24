package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a {@code LootCondition} that wraps another condition and inverts the result.
 */
public class InvertedCondition implements LootCondition {
    /**
     * The immutable key for all {@code InvertedCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "inverted");

    private final LootCondition condition;

    /**
     * Initialize an InvertedCondition with the provided condition
     */
    public InvertedCondition(@NotNull LootCondition condition){
        this.condition = condition;
    }

    /**
     * Returns the condition that gets inverted
     */
    public @NotNull LootCondition condition(){
        return condition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
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

    @Override
    public String toString() {
        return "InvertedCondition[condition=" + condition + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvertedCondition that = (InvertedCondition) o;
        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return condition.hashCode() * 37;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code InvertedCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new InvertedCondition(loader.getLootConditionManager().deserialize(json.get("term"), "term"));
    }
}