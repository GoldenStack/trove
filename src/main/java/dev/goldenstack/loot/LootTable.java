package dev.goldenstack.loot;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootParameterGroup;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonHelper;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that stores a list of loot pools, loot functions, and a required parameter group. This can be used to
 * generate loot.
 */
public record LootTable(@Nullable LootParameterGroup group, @NotNull ImmutableList<LootPool> pools, @NotNull ImmutableList<LootFunction> functions) {
    /**
     * An empty loot table that always generates no loot.
     */
    public static final @NotNull LootTable EMPTY = new LootTable(LootParameterGroup.EMPTY, ImmutableList.of(), ImmutableList.of());

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
    public @NotNull JsonObject serialize(@NotNull ImmuTables loader){
        JsonObject object = new JsonObject();

        if (this.group != null){
            String key = loader.getLootParameterGroupRegistry().inverse().get(this.group);
            if (key != null){
                object.addProperty("type", key);
            }
        }

        JsonArray poolsArray = new JsonArray();
        for (LootPool pool : this.pools){
            poolsArray.add(pool.serialize(loader));
        }
        object.add("pools", poolsArray);

        JsonArray functionsArray = new JsonArray();
        for (LootFunction function : this.functions){
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

            group = loader.getLootParameterGroupRegistry().get(groupName);
            if (group == null) {
                throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid parameter group", "type", null));
            }
        }

        JsonElement rawPools = object.get("pools");
        ImmutableList<LootPool> pools;
        if (rawPools == null) {
            pools = ImmutableList.of();
        } else {
            JsonArray jsonPools = JsonHelper.assureJsonArray(rawPools, "pools");
            ImmutableList.Builder<LootPool> poolsBuilder = ImmutableList.builder();
            for (JsonElement element : jsonPools) {
                JsonObject jsonObject = JsonHelper.assureJsonObject(element, "pools (while deserializing an element)");
                poolsBuilder.add(LootPool.deserialize(jsonObject, loader));
            }
            pools = poolsBuilder.build();
        }

        JsonElement rawFunctions = object.get("functions");
        ImmutableList<LootFunction> functions;
        if (rawFunctions == null) {
            functions = ImmutableList.of();
        } else {
            JsonArray jsonFunctions = JsonHelper.assureJsonArray(rawFunctions, "functions");
            ImmutableList.Builder<LootFunction> functionsBuilder = ImmutableList.builder();
            for (JsonElement element : jsonFunctions) {
                functionsBuilder.add(loader.getLootFunctionManager().deserialize(element, "functions (while deserializing an element)"));
            }
            functions = functionsBuilder.build();
        }

        return new LootTable(group, pools, functions);
    }

}
