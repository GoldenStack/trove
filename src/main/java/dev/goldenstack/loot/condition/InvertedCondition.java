package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that wraps another condition and inverts the result.
 */
public record InvertedCondition(@NotNull LootCondition condition) implements LootCondition {

    public static final @NotNull JsonLootConverter<InvertedCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:inverted"), InvertedCondition.class) {
        @Override
        public @NotNull InvertedCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new InvertedCondition(loader.getLootConditionManager().deserialize(json.get("term"), "term"));
        }

        @Override
        public void serialize(@NotNull InvertedCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("term", loader.getLootConditionManager().serialize(input.condition));
        }
    };

    /**
     * Returns the inverted result of this instance's {@link #condition}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return !condition.test(context);
    }
}