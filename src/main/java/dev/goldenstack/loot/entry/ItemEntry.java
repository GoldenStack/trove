package dev.goldenstack.loot.entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that always generates an item with the same material.
 */
public class ItemEntry extends ConstantChoiceEntry {

    public static final @NotNull JsonLootConverter<ItemEntry> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:item"), ItemEntry.class) {
        @Override
        public @NotNull ItemEntry deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            JsonElement name = json.get("name");
            Material material = Material.fromNamespaceId(JsonHelper.assureNamespaceId(name, "name"));

            if (material == null) {
                throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid material (as a NamespaceID)", "name", name));
            }

            return new ItemEntry(
                    LootEntry.deserializeConditions(json, loader),
                    LootEntry.deserializeFunctions(json, loader),
                    LootEntry.deserializeWeight(json, loader),
                    LootEntry.deserializeQuality(json, loader),
                    material
            );
        }

        @Override
        public void serialize(@NotNull ItemEntry input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            LootEntry.serializeLootEntry(input, result, loader);
            result.addProperty("name", input.material.namespace().asString());
        }
    };

    private final @NotNull Material material;

    /**
     * Initializes an ItemEntry instance with the provided material
     */
    public ItemEntry(@NotNull List<LootCondition> conditions, @NotNull List<LootFunction> functions, int weight,
                     int quality, @NotNull Material material) {
        super(conditions, functions, weight, quality);
        this.material = material;
    }

    /**
     * Returns the material that will be used to generate items.
     */
    public @NotNull Material material() {
        return material;
    }

    /**
     * Generates a list with one item of the material {@link #material()}.
     */
    @Override
    public @NotNull List<ItemStack> generateLoot(@NotNull LootContext context) {
        return List.of(ItemStack.of(material));
    }

    @Override
    public String toString() {
        return "ItemEntry[" + LootEntry.partialToString(this) + ", material=" + material + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ItemEntry itemEntry = (ItemEntry) o;
        return material.equals(itemEntry.material);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + material.hashCode();
    }
}
