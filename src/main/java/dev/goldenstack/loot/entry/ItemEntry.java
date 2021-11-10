package dev.goldenstack.loot.entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that always generates an item with the same material.
 */
public class ItemEntry extends ConstantChoiceEntry {
    /**
     * The immutable key for all ItemEntry instances
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "item");

    private final @NotNull Material material;

    /**
     * Initializes an ItemEntry instance with the provided material
     */
    public ItemEntry(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions,
                     int weight, int quality, @NotNull Material material) {
        super(conditions, functions, weight, quality);
        this.material = material;
    }

    /**
     * Returns the material that will be used to generate items.
     */
    public @NotNull Material material(){
        return material;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        super.serialize(object, loader);
        object.addProperty("name", this.material.namespace().asString());
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
     * Generates a list with one item of the material {@link #material()}.
     */
    @Override
    public @NotNull List<ItemStack> generateLoot(@NotNull LootContext context) {
        return ImmutableList.of(ItemStack.of(material));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootEntry>> getDeserializer() {
        return ItemEntry::deserialize;
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

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code ItemEntry}.
     */
    public static @NotNull LootEntry deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader){
        JsonElement name = object.get("name");
        Material material = Material.fromNamespaceId(JsonHelper.assureNamespaceId(name, "name"));

        if (material == null){
            throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid material (as a NamespaceID)", "name", name));
        }

        return new ItemEntry(
                LootEntry.deserializeConditions(object, loader),
                LootEntry.deserializeFunctions(object, loader),
                LootEntry.deserializeWeight(object, loader),
                LootEntry.deserializeQuality(object, loader),
                material
        );
    }
}
