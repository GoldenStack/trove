package dev.goldenstack.loot.entry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that generates nothing.
 */
public class EmptyEntry extends ConstantChoiceEntry {

    public static final @NotNull JsonLootConverter<EmptyEntry> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:empty"), EmptyEntry.class) {
        @Override
        public @NotNull EmptyEntry deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new EmptyEntry(
                    LootEntry.deserializeConditions(json, loader),
                    LootEntry.deserializeFunctions(json, loader),
                    LootEntry.deserializeWeight(json, loader),
                    LootEntry.deserializeQuality(json, loader)
            );
        }

        @Override
        public void serialize(@NotNull EmptyEntry input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            LootEntry.serializeLootEntry(input, result, loader);
        }
    };

    /**
     * Initializes an EmptyEntry instance
     */
    public EmptyEntry(@NotNull List<LootCondition> conditions, @NotNull List<LootFunction> functions, int weight,
                      int quality) {
        super(conditions, functions, weight, quality);
    }

    /**
     * Generates an empty list
     */
    @Override
    public @NotNull List<ItemStack> generateLoot(@NotNull LootContext context) {
        return List.of();
    }

    @Override
    public String toString() {
        return "EmptyEntry[" + LootEntry.partialToString(this) + "]";
    }
}
