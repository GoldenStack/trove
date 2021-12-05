package dev.goldenstack.loot;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.entry.LootEntry;
import dev.goldenstack.loot.function.LootFunction;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.provider.number.ConstantNumber;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A loot pool is a collection of loot entries, guarded by a list of conditions and modified by a list of functions,
 * that gets run a selected number of times based on the rolls plus the context's luck multiplied by the value of
 * bonusRolls.
 */
public record LootPool(@NotNull ImmutableList<LootEntry> entries, @NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions,
                       @NotNull NumberProvider rolls, @NotNull NumberProvider bonusRolls) {

    /**
     * Generates a list of items as loot.<br>
     * The steps for generating the loot are as follows:
     * <ul>
     *     <li>If the {@link #conditions()} do not all accept the provided context, return an empty list.</li>
     *     <li>Generate the number of rolls as the result of {@code rolls().getInt(context) + floor(bonusRolls.
     *     getDouble(context) * context.luck())}</li>
     *     <li>For each roll, pick a random entry from {@link #entries()} and add its result to a list of items</li>
     *     <li>Apply each function from {@link #functions()} to the list of items sequentially.</li>
     *     <li>Return the list of items</li>
     * </ul>
     */
    public @NotNull List<ItemStack> generateLoot(@NotNull LootContext context) {
        if (!LootCondition.and(context, this.conditions)) {
            return List.of();
        }

        List<ItemStack> items = new ArrayList<>();

        int rolls = this.rolls.getInt(context) + (int) Math.floor(this.bonusRolls.getDouble(context) * context.luck());

        for (int i = 0; i < rolls; i++) {
            int total = 0;
            List<LootEntry.Choice> choices = new ArrayList<>();
            for (LootEntry entry : this.entries) {
                choices.addAll(entry.getChoices(context));
            }

            if (choices.size() == 0) {
                continue;
            }
            if (choices.size() == 1) {
                items.addAll(choices.get(0).generateLoot(context));
                continue;
            }

            for (LootEntry.Choice choice : choices) {
                total += choice.getWeight(context.luck());
            }
            if (total == 0) {
                continue;
            }

            int value = context.findRandom().nextInt(total);

            Iterator<LootEntry.Choice> iterator = choices.iterator();
            LootEntry.Choice choice = null;
            do {
                if (!iterator.hasNext()) {
                    break;
                }
                choice = iterator.next();
                value -= choice.getWeight(context.luck());
            } while (value >= 0);

            if (choice != null) {
                items.addAll(choice.generateLoot(context));
            }

        }

        List<ItemStack> appliedItems = new ArrayList<>();
        for (ItemStack item : items) {
            for (LootFunction function : this.functions) {
                item = function.apply(item, context);
            }
            appliedItems.add(item);
        }

        return appliedItems;
    }

    /**
     * Serializes this {@code LootPool} into a new {@code JsonObject}
     */
    @Contract("_ -> new")
    public @NotNull JsonObject serialize(@NotNull ImmuTables loader) {
        JsonObject object = new JsonObject();
        object.add("rolls", loader.getNumberProviderManager().serialize(this.rolls));
        if (!(this.bonusRolls instanceof ConstantNumber constantNumber) || (constantNumber.value() == 0)) {
            object.add("bonusRolls", loader.getNumberProviderManager().serialize(this.bonusRolls));
        }

        if (this.entries.size() > 0) {
            JsonArray array = new JsonArray();
            for (LootEntry entry : this.entries) {
                array.add(loader.getLootEntryManager().serialize(entry));
            }
            object.add("entries", array);
        }

        if (this.conditions.size() > 0) {
            JsonArray array = new JsonArray();
            for (LootCondition condition : this.conditions) {
                array.add(loader.getLootConditionManager().serialize(condition));
            }
            object.add("conditions", array);
        }

        if (this.functions.size() > 0) {
            JsonArray array = new JsonArray();
            for (LootFunction function : this.functions) {
                array.add(loader.getLootFunctionManager().serialize(function));
            }
            object.add("functions", array);
        }

        return object;
    }

    /**
     * Deserializes the provided {@code JsonObject} into a {@code LootPool}.
     */
    @Contract("_, _ -> new")
    public static @NotNull LootPool deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader) {
        NumberProvider rolls = loader.getNumberProviderManager().deserialize(object.get("rolls"), "rolls");

        JsonElement jsonBonusRolls = object.get("bonus_rolls");
        NumberProvider bonusRolls = JsonHelper.isNull(jsonBonusRolls) ?
                new ConstantNumber(0) :
                loader.getNumberProviderManager().deserialize(object.get("bonus_rolls"), "bonus_rolls");

        JsonElement jsonEntries = object.get("entries");
        ImmutableList<LootEntry> entries = JsonHelper.isNull(jsonEntries) ?
                ImmutableList.of() :
                JsonHelper.deserializeJsonArray(jsonEntries, "entries", loader.getLootEntryManager()::deserialize);

        JsonElement jsonConditions = object.get("conditions");
        ImmutableList<LootCondition> conditions = JsonHelper.isNull(jsonConditions) ?
                ImmutableList.of() :
                JsonHelper.deserializeJsonArray(jsonConditions, "conditions", loader.getLootConditionManager()::deserialize);

        JsonElement jsonFunctions = object.get("functions");
        ImmutableList<LootFunction> functions = JsonHelper.isNull(jsonFunctions) ?
                ImmutableList.of() :
                JsonHelper.deserializeJsonArray(jsonFunctions, "functions", loader.getLootFunctionManager()::deserialize);

        return new LootPool(entries, conditions, functions, rolls, bonusRolls);
    }
}
