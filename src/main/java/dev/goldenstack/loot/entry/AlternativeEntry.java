package dev.goldenstack.loot.entry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that returns the result of the first child that passes its conditions
 */
public class AlternativeEntry extends CombinedEntry {

    /**
     * Initialize a new AlternativeEntry with the provided conditions, functions, weight, quality, and children.
     */
    public AlternativeEntry(@NotNull List<LootCondition> conditions, @NotNull List<LootFunction> functions, int weight,
                            int quality, @NotNull List<LootEntry> children) {
        super(conditions, functions, weight, quality, children);
    }

    /**
     * Returns the result of the first child that passes its conditions
     */
    @Override
    protected @NotNull List<Choice> collectChoices(@NotNull LootContext context) {
        for (LootEntry entry : this.children()) {
            var choices = entry.getChoices(context);
            if (choices.size() > 0) {
                return choices;
            }
        }
        return List.of();
    }

    @Override
    public String toString() {
        return "AlternativeEntry[" + CombinedEntry.partialToString(this) + "]";
    }

    public static final @NotNull JsonLootConverter<AlternativeEntry> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:alternatives"), AlternativeEntry.class) {
        @Override
        public @NotNull AlternativeEntry deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new AlternativeEntry(
                    LootEntry.deserializeConditions(json, loader),
                    LootEntry.deserializeFunctions(json, loader),
                    LootEntry.deserializeWeight(json, loader),
                    LootEntry.deserializeQuality(json, loader),
                    CombinedEntry.deserializeChildren(json, loader)
            );
        }

        @Override
        public void serialize(@NotNull AlternativeEntry input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            CombinedEntry.serializeCombinedEntry(input, result, loader);
        }
    };
}
