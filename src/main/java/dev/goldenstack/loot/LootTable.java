package dev.goldenstack.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootParameterGroup;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonHelper;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that stores a list of loot pools, loot functions, and a required parameter group. This can be used to
 * generate loot.
 */
public record LootTable(@Nullable LootParameterGroup group, @NotNull List<LootPool> pools, @NotNull List<LootFunction> functions) {

    /**
     * An empty loot table that consistently generates nothing.
     */
    public static final @NotNull LootTable EMPTY = new LootTable(LootParameterGroup.EMPTY, List.of(), List.of());

    public LootTable {
        pools = List.copyOf(pools);
        functions = List.copyOf(functions);
    }

    /**
     * Generates a list of items from this LootTable. First, the items from the pools are added, then all the functions
     * are applied.
     */
    public @NotNull List<ItemStack> generateLoot(@NotNull LootContext context) {
        List<ItemStack> items = new ArrayList<>();

        if (this.pools.size() > 0) {
            for (LootPool pool : this.pools) {
                items.addAll(pool.generateLoot(context));
            }
        }

        if (this.functions.size() > 0 && items.size() > 0) {
            List<ItemStack> newItems = new ArrayList<>();
            for (ItemStack item : items) {
                for (LootFunction function : this.functions) {
                    item = function.apply(item, context);
                }
                newItems.add(item);
            }
            items = newItems;
        }

        return items;
    }

    /**
     * Serializes this LootTable into a new JsonObject instance.
     */
    @Contract("_ -> new")
    public @NotNull JsonObject serialize(@NotNull ImmuTables loader) {
        JsonObject object = new JsonObject();

        if (this.group != null) {
            object.addProperty("type", this.group.key().asString());
        }

        JsonArray poolsArray = new JsonArray();
        for (LootPool pool : this.pools) {
            poolsArray.add(pool.serialize(loader));
        }
        object.add("pools", poolsArray);

        JsonArray functionsArray = new JsonArray();
        for (LootFunction function : this.functions) {
            functionsArray.add(loader.getLootFunctionManager().serialize(function));
        }
        object.add("functions", functionsArray);

        return object;
    }

    /**
     * Deserializes the provided {@code JsonObject} into a new {@code LootTable} instance.
     */
    @Contract("_, _ -> new")
    public static @NotNull LootTable deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader) {
        LootParameterGroup group = null;
        GroupDetection:
        {
            JsonElement groupElement = object.get("type");
            if (JsonHelper.isNull(groupElement)) {
                break GroupDetection;
            }

            String groupName = JsonHelper.assureString(object.get("type"), "type");

            group = loader.getLootParameterGroupRegistry().get(NamespaceID.from(groupName));
            if (group == null) {
                throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid parameter group", "type", null));
            }
        }

        List<LootPool> pools = List.of();
        if (object.has("pools")) {
            JsonArray jsonPools = JsonHelper.assureJsonArray(object.get("pools"), "pools");
            pools = new ArrayList<>();
            for (JsonElement element : jsonPools) {
                JsonObject jsonObject = JsonHelper.assureJsonObject(element, "pools (while deserializing an element)");
                pools.add(LootPool.deserialize(jsonObject, loader));
            }
        }

        List<LootFunction> functions = List.of();
        if (object.has("functions")) {
            JsonArray jsonFunctions = JsonHelper.assureJsonArray(object.get("functions"), "functions");
            functions = new ArrayList<>();
            for (JsonElement element : jsonFunctions) {
                functions.add(loader.getLootFunctionManager().deserialize(element, "functions (while deserializing an element)"));
            }
        }

        return new LootTable(group, pools, functions);
    }

}
