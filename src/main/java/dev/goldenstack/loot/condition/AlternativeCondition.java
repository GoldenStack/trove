package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a {@code LootCondition} that returns true if at least one of this instance's conditions is true according
 * to {@link LootCondition#or(LootContext, List)}.
 */
public record AlternativeCondition(@NotNull List<LootCondition> terms) implements LootCondition {

    public AlternativeCondition {
        terms = List.copyOf(terms);
    }

    /**
     * Returns true if {@link LootCondition#or(LootContext, List)} returns true for {@link #terms()}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return LootCondition.or(context, this.terms);
    }

    public static final @NotNull JsonLootConverter<AlternativeCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:alternative"), AlternativeCondition.class) {
        @Override
        public @NotNull AlternativeCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new AlternativeCondition(JsonHelper.deserializeJsonArray(json.get("terms"), "terms", loader.getLootConditionManager()::deserialize));
        }

        @Override
        public void serialize(@NotNull AlternativeCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("terms", JsonHelper.serializeJsonArray(input.terms, loader.getLootConditionManager()::serialize));
        }
    };
}