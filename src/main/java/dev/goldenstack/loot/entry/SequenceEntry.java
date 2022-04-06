package dev.goldenstack.loot.entry;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry that returns the combined result of all of the children that pass their conditions until there is a child
 * that does not pass its conditions.
 */
public class SequenceEntry extends CombinedEntry {
    /**
     * The immutable key for all SequenceEntry instances
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "sequence");

    /**
     * Initialize a new SequenceEntry with the provided conditions, functions, weight, quality, and children.
     */
    public SequenceEntry(@NotNull List<LootCondition> conditions, @NotNull List<LootFunction> functions, int weight,
                         int quality, @NotNull List<LootEntry> children) {
        super(conditions, functions, weight, quality, children);
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
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootEntry>> getDeserializer() {
        return SequenceEntry::deserialize;
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

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code SequenceEntry}.
     */
    public static @NotNull LootEntry deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader) {
        return new SequenceEntry(
                LootEntry.deserializeConditions(object, loader),
                LootEntry.deserializeFunctions(object, loader),
                LootEntry.deserializeWeight(object, loader),
                LootEntry.deserializeQuality(object, loader),
                CombinedEntry.deserializeChildren(object, loader)
        );
    }

}