package dev.goldenstack.loot.entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that generates nothing.
 */
public class EmptyEntry extends ConstantChoiceEntry {
    /**
     * The immutable key for all EmptyEntry instances
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "empty");

    /**
     * Initializes an EmptyEntry instance
     */
    public EmptyEntry(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions, int weight, int quality) {
        super(conditions, functions, weight, quality);
    }

    /**
     * Generates an empty list
     */
    @Override
    public @NotNull List<ItemStack> generateLoot(@NotNull LootContext context) {
        return ImmutableList.of();
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
        return EmptyEntry::deserialize;
    }

    @Override
    public String toString() {
        return "EmptyEntry[" + LootEntry.partialToString(this) + "]";
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code EmptyEntry}.
     */
    public static @NotNull LootEntry deserialize(@NotNull JsonObject object, @NotNull LootTableLoader loader){
        return new EmptyEntry(
                LootEntry.deserializeConditions(object, loader),
                LootEntry.deserializeFunctions(object, loader),
                LootEntry.deserializeWeight(object, loader),
                LootEntry.deserializeQuality(object, loader)
        );
    }
}
