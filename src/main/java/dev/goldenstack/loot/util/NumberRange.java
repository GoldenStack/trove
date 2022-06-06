package dev.goldenstack.loot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.provider.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for representing ranges of numbers, with optional minimum and maximum values
 */
public record NumberRange(@Nullable NumberProvider min, @Nullable NumberProvider max) {

    /**
     * Limits {@code number} to a number between {@link #min()} and {@link #max()}. Null fields are not taken into
     * account, so, for example, if {@code min} is null, the number will essentially only have a maximum value. If both
     * {@code min} and {@code max} are null, {@code number} is always returned.
     */
    public int limit(@NotNull LootContext context, int number) {
        if (this.min != null) {
            number = Math.max(this.min.getInteger(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getInteger(context), number);
        }
        return number;
    }

    /**
     * Limits {@code number} to a number between {@link #min()} and {@link #max()}. Null fields are not taken into
     * account, so, for example, if {@code min} is null, the number will essentially only have a maximum value. If both
     * {@code min} and {@code max} are null, {@code number} is always returned.
     */
    public double limit(@NotNull LootContext context, double number) {
        if (this.min != null) {
            number = Math.max(this.min.getDouble(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getDouble(context), number);
        }
        return number;
    }

    /**
     * Returns true if the number is greater than or equal to {@link #min()} and less than or equal to {@link #max()}.
     * Null fields are not taken into account, so, for example, if {@code min} is null, any numbers less than or
     * equal to {@code max} are valid. If both {@code min} and {@code max} are null, true is always returned.
     */
    public boolean predicate(@NotNull LootContext context, int number) {
        return (this.min == null || this.min.getInteger(context) <= number) && (this.max == null || this.max.getInteger(context) >= number);
    }

    /**
     * Returns true if the number is greater than or equal to {@link #min()} and less than or equal to {@link #max()}.
     * Null fields are not taken into account, so, for example, if {@code min} is null, any numbers less than or
     * equal to {@code max} are valid. If both {@code min} and {@code max} are null, true is always returned.
     */
    public boolean predicate(@NotNull LootContext context, double number) {
        return (this.min == null || this.min.getDouble(context) <= number) && (this.max == null || this.max.getDouble(context) >= number);
    }

    /**
     * Serializes this NumberRange to a JsonObject. Null fields will not be added to the JsonObject, so it is possible
     * for this method to return an empty JsonObject if {@link #min()} and {@link #max()} are both null.
     */
    public @NotNull JsonObject serialize(@NotNull ImmuTables loader) throws JsonParseException {
        JsonObject object = new JsonObject();

        if (this.min != null) {
            object.add("min", loader.getNumberProviderManager().serialize(this.min));
        }
        if (this.max != null) {
            object.add("max", loader.getNumberProviderManager().serialize(this.max));
        }

        return object;
    }

    /**
     * Deserializes the provided JsonObject into a NumberRange
     */
    public static @NotNull NumberRange deserialize(@NotNull ImmuTables loader, @Nullable JsonElement element, @Nullable String key) throws JsonParseException {
        JsonObject json = JsonHelper.assureJsonObject(element, key);

        JsonElement minElement = json.get("min"), maxElement = json.get("max");
        NumberProvider min = null, max = null;

        if (minElement != null) {
            min = loader.getNumberProviderManager().deserialize(JsonHelper.assureJsonObject(minElement, "min"), "min");
        }
        if (maxElement != null) {
            max = loader.getNumberProviderManager().deserialize(JsonHelper.assureJsonObject(maxElement, "max"), "max");
        }

        return new NumberRange(min, max);
    }
}