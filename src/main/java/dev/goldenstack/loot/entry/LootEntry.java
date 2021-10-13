package dev.goldenstack.loot.entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootChoice;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An entry that can contain conditions. This is the most basic entry class, so it should be the one that gets used if
 * you want to cover all possible entries.
 */
public abstract class LootEntry implements LootSerializer<LootEntry> {

    private final @NotNull ImmutableList<LootCondition> conditions;

    /**
     * Creates a new LootEntry with the provided conditions
     */
    public LootEntry(@NotNull ImmutableList<LootCondition> conditions){
        this.conditions = conditions;
    }

    /**
     * Returns this LootEntry's conditions
     */
    public final @NotNull ImmutableList<LootCondition> conditions(){
        return conditions;
    }

    /**
     * {@inheritDoc}<br>
     * If you want to add more information to the JsonObject, it is a good idea to override this method, but make sure
     * to run {@code super.serialize(object, loader)} so that the fields can get serialized!
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        if (this.conditions.size() > 0){
            object.add("conditions", JsonHelper.serializeJsonArray(this.conditions, loader.getLootConditionManager()::serialize));
        }
    }

    /**
     * Returns the list of {@code LootChoice}s that this instance holds.
     */
    public abstract @NotNull ImmutableList<LootChoice> getChoices(@NotNull LootContext context);

    /**
     * Tests this entry's conditions against the provided LootContext.
     */
    public final boolean testConditions(@NotNull LootContext context){
        return LootCondition.and(context, this.conditions);
    }

    @Override
    public String toString() {
        return "LootEntry[conditions=" + conditions + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootEntry that = (LootEntry) o;
        return conditions.equals(that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(conditions);
    }

    /**
     * Utility method for getting an immutable list of the conditions from the JsonObject.<br>
     * This should be called in a similar manner to: <br>
     * {@code ImmutableList<LootCondition> conditions = LootEntry.deserializeConditions(json, loader);}
     */
    public static @NotNull ImmutableList<LootCondition> deserializeConditions(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        JsonElement functions = json.get("conditions");
        if (JsonHelper.isNull(functions)){
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(JsonHelper.deserializeJsonArray(functions, "conditions", loader.getLootConditionManager()::deserialize));
    }
}