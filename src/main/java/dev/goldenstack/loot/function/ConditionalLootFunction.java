package dev.goldenstack.loot.function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the basic information for a LootFunction that can be given a list of functions. The LootFunction's effects
 * are only applied to the item if all the functions return true for the provided LootContext.<br>
 * This should be used instead of directly implementing LootFunction, although implementations that don't extend this
 * are fully supported.
 */
public abstract class ConditionalLootFunction implements LootFunction {

    private final @NotNull List<LootCondition> conditions;

    /**
     * Creates a ConditionalLootFunction with the provided {@code LootCondition}s
     */
    public ConditionalLootFunction(@NotNull List<LootCondition> conditions) {
        this.conditions = List.copyOf(conditions);
    }

    /**
     * Returns this ConditionalLootFunction's conditions
     */
    public final @NotNull List<LootCondition> conditions() {
        return conditions;
    }

    /**
     * Applies this LootFunction to the provided ItemStack. If all the conditions from {@link #conditions()} return true
     * for the provided LootContext, {@link #modify(ItemStack, LootContext)} is applied.
     */
    @Override
    public final @NotNull ItemStack apply(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        if (!LootCondition.and(context, this.conditions)){
            return itemStack;
        }
        return modify(itemStack, context);
    }

    /**
     * Applies this LootFunction to the provided ItemStack and returns the result.
     */
    public abstract @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context);

    /**
     * Utility method for getting an immutable list of the conditions from the JsonObject.<br>
     * This should be called in a similar manner to: <br>
     * {@code List<LootCondition> conditions = ConditionalLootFunction.deserializeConditions(json, loader);}
     */
    public static @NotNull List<LootCondition> deserializeConditions(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        JsonElement functions = json.get("conditions");
        if (JsonHelper.isNull(functions)){
            return List.of();
        }
        return JsonHelper.deserializeJsonArray(functions, "conditions", loader.getLootConditionManager()::deserialize);
    }

    /**
     * Serializes the conditions in the provided function to the provided JsonObject.
     */
    public static void serializeConditionalLootFunction(@NotNull ConditionalLootFunction input, @NotNull JsonObject result,
                                                        @NotNull ImmuTables loader) throws JsonParseException {
        if (input.conditions.size() > 0) {
            result.add("conditions", JsonHelper.serializeJsonArray(input.conditions, loader.getLootConditionManager()::serialize));
        }
    }
}
