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

import java.util.ArrayList;
import java.util.List;

/**
 * An entry that returns the combined result of all of the children that pass their conditions until there is a child
 * that does not pass its conditions.
 */
public class SequenceEntry extends CombinedEntry {

    public static final @NotNull JsonLootConverter<SequenceEntry> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:sequence"), SequenceEntry.class) {
        @Override
        public @NotNull SequenceEntry deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new SequenceEntry(
                    LootEntry.deserializeConditions(json, loader),
                    LootEntry.deserializeFunctions(json, loader),
                    LootEntry.deserializeWeight(json, loader),
                    LootEntry.deserializeQuality(json, loader),
                    CombinedEntry.deserializeChildren(json, loader)
            );
        }

        @Override
        public void serialize(@NotNull SequenceEntry input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            CombinedEntry.serializeCombinedEntry(input, result, loader);
        }
    };

    /**
     * Initialize a new SequenceEntry with the provided conditions, functions, weight, quality, and children.
     */
    public SequenceEntry(@NotNull List<LootCondition> conditions, @NotNull List<LootFunction> functions, int weight,
                         int quality, @NotNull List<LootEntry> children) {
        super(conditions, functions, weight, quality, children);
    }

    /**
     * Returns the combined results of all of the children that pass their conditions until there is a child
     * that does not pass its conditions.
     */
    @Override
    protected @NotNull List<Choice> collectChoices(@NotNull LootContext context) {
        List<Choice> choices = new ArrayList<>();
        for (LootEntry entry : this.children()){
            var entryChoices = entry.getChoices(context);
            if (entryChoices.size() == 0) {
                break;
            }
        }
        return List.copyOf(choices);
    }

    @Override
    public String toString() {
        return "SequenceEntry[" + CombinedEntry.partialToString(this) + "]";
    }
}