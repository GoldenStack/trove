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
 * An entry that returns the combined results of all its children.
 */
public class GroupEntry extends CombinedEntry {

    public static final @NotNull JsonLootConverter<GroupEntry> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:group"), GroupEntry.class) {
        @Override
        public @NotNull GroupEntry deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new GroupEntry(
                    LootEntry.deserializeConditions(json, loader),
                    LootEntry.deserializeFunctions(json, loader),
                    LootEntry.deserializeWeight(json, loader),
                    LootEntry.deserializeQuality(json, loader),
                    CombinedEntry.deserializeChildren(json, loader)
            );
        }

        @Override
        public void serialize(@NotNull GroupEntry input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            CombinedEntry.serializeCombinedEntry(input, result, loader);
        }
    };

    /**
     * Initialize a new GroupEntry with the provided conditions, functions, weight, quality, and children.
     */
    public GroupEntry(@NotNull List<LootCondition> conditions, @NotNull List<LootFunction> functions, int weight,
                      int quality, @NotNull List<LootEntry> children) {
        super(conditions, functions, weight, quality, children);
    }

    /**
     * Returns the combined results from all of this entry's children.
     */
    @Override
    protected @NotNull List<Choice> collectChoices(@NotNull LootContext context) {
        List<Choice> choices = new ArrayList<>();
        for (LootEntry entry : this.children()){
            choices.addAll(entry.getChoices(context));
        }
        return List.copyOf(choices);
    }

    @Override
    public String toString() {
        return "GroupEntry[" + CombinedEntry.partialToString(this) + "]";
    }
}
