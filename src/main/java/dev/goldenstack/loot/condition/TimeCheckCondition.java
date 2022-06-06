package dev.goldenstack.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext's instance has a time that fits in
 * this {@link #range()}'s values. If {@link #period()}, is not null, the result of {@code time % this.period.longValue()}
 * is used as the time instead of the raw value.
 */
public record TimeCheckCondition(@NotNull NumberRange range, @Nullable Number period) implements LootCondition {

    /**
     * Returns true if the context's instance has a time that fits in the range of {@link #range()}. If {@link #period()}
     * is not null, the time is calculated with {@code time % period.longValue()}.<br>
     * If the context does not have an instance, false is always returned.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        Instance instance = context.instance();
        if (instance == null) {
            return false;
        }
        long value = instance.getTime();
        if (this.period != null) {
            value %= this.period.longValue();
        }
        return this.range.predicate(context, value);
    }

    public static final @NotNull JsonLootConverter<TimeCheckCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:time_check"), TimeCheckCondition.class) {
        @Override
        public @NotNull TimeCheckCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            NumberRange range = JsonHelper.optionalAlternativeKey(json, (element, key) -> NumberRange.deserialize(loader, element, key), "range", "value");

            JsonElement number = json.get("period");
            Number period = null;
            if (number != null) {
                period = JsonHelper.assureNumber(number, "period");
            }

            return new TimeCheckCondition(range, period);
        }

        @Override
        public void serialize(@NotNull TimeCheckCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            if (input.period != null) {
                result.addProperty("period", input.period);
            }
            result.add("range", input.range.serialize(loader));
        }
    };
}

