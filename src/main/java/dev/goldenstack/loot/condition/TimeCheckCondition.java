package dev.goldenstack.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
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
     * The immutable key for all {@code TimeCheckCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "time_check");

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        if (this.period != null) {
            object.addProperty("period", this.period);
        }
        object.add("range", this.range.serialize(loader));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return TimeCheckCondition::deserialize;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code TimeCheckCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        NumberRange range = JsonHelper.optionalAlternativeKey(json, loader::deserializeNumberRange, "range", "value");

        JsonElement number = json.get("period");
        Number period = null;
        if (number != null) {
            period = JsonHelper.assureNumber(number, "period");
        }

        return new TimeCheckCondition(range, period);
    }
}

