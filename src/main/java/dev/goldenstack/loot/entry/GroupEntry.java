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
 * An entry that returns the combined results of all its children.
 */
public class GroupEntry extends CombinedEntry {
    /**
     * The immutable key for all GroupEntry instances
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "group");

    /**
     * Initialize a new GroupEntry with the provided conditions, functions, weight, quality, and children.
     */
    public GroupEntry(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions,
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
        return GroupEntry::deserialize;
    }

    /**
     * Returns the combined results from all of this entry's children.
     */
    @Override
    protected @NotNull ImmutableList<Choice> collectChoices(@NotNull LootContext context) {
        ImmutableList.Builder<Choice> choice = ImmutableList.builder();
        for (LootEntry entry : this.children()){
            choice.addAll(entry.getChoices(context));
        }
        return choice.build();
    }

    @Override
    public String toString() {
        return "GroupEntry[" + CombinedEntry.partialToString(this) + "]";
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code GroupEntry}.
     */
    public static @NotNull LootEntry deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader){
        return new GroupEntry(
                LootEntry.deserializeConditions(object, loader),
                LootEntry.deserializeFunctions(object, loader),
                LootEntry.deserializeWeight(object, loader),
                LootEntry.deserializeQuality(object, loader),
                CombinedEntry.deserializeChildren(object, loader)
        );
    }
}
