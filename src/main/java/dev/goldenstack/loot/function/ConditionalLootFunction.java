package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the basic information for a LootFunction that can be given a list of functions. The LootFunction's effects
 * are only applied to the item if all the functions return true for the provided LootContext.<br>
 * This should be used instead of directly implementing LootFunction, although implementations that don't extend this
 * are fully supported.
 */
public abstract class ConditionalLootFunction implements LootFunction {

    private final @NotNull ImmutableList<LootCondition> conditions;

    /**
     * Creates a ConditionalLootFunction with the provided {@code LootCondition}s
     */
    public ConditionalLootFunction(@NotNull ImmutableList<LootCondition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Returns this ConditionalLootFunction's conditions
     */
    public final @NotNull ImmutableList<LootCondition> conditions() {
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
     * {@inheritDoc}<br>
     * If you want to add more information to the JsonObject, it is a good idea to override this method, but make sure
     * to run {@code super.serialize(object, loader)} so that the conditions can get serialized!
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        if (this.conditions.size() > 0) {
            object.add("conditions", JsonHelper.serializeJsonArray(this.conditions, loader.getLootConditionManager()::serialize));
        }
    }

    /**
     * Utility method for getting an immutable list of the conditions from the JsonObject.<br>
     * This should be called in a similar manner to: <br>
     * {@code ImmutableList<LootCondition> conditions = ConditionalLootFunction.deserializeConditions(json, loader);}
     */
    public static @NotNull ImmutableList<LootCondition> deserializeConditions(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        JsonElement functions = json.get("conditions");
        if (JsonHelper.isNull(functions)){
            return ImmutableList.of();
        }
        return JsonHelper.deserializeJsonArray(functions, "conditions", loader.getLootConditionManager()::deserialize);
    }
}
