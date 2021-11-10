package dev.goldenstack.loot.entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * An entry that returns the result of the first child that passes its conditions
 */
public class AlternativeEntry extends CombinedEntry {
    /**
     * The immutable key for all AlternativeEntry instances
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "alternatives");

    /**
     * Initialize a new AlternativeEntry with the provided conditions, functions, weight, quality, and children.
     */
    public AlternativeEntry(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions,
                      int weight, int quality, @NotNull ImmutableList<LootEntry> children){
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
        return AlternativeEntry::deserialize;
    }

    /**
     * Returns the result of the first child that passes its conditions
     */
    @Override
    protected @NotNull ImmutableList<Choice> collectChoices(@NotNull LootContext context) {
        for (LootEntry entry : this.children()){
            var choices = entry.getChoices(context);
            if (choices.size() > 0){
                return choices;
            }
        }
        return ImmutableList.of();
    }

    @Override
    public String toString() {
        return "AlternativeEntry[" + CombinedEntry.partialToString(this) + "]";
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code AlternativeEntry}.
     */
    public static @NotNull LootEntry deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader){
        return new AlternativeEntry(
                LootEntry.deserializeConditions(object, loader),
                LootEntry.deserializeFunctions(object, loader),
                LootEntry.deserializeWeight(object, loader),
                LootEntry.deserializeQuality(object, loader),
                CombinedEntry.deserializeChildren(object, loader)
        );
    }
}
