package dev.goldenstack.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext's instance has a time that fits in
 * this {@link #range()}'s values. If {@link #period()}, is not null, the result of {@code time % this.period.longValue()}
 * is used as the time instead of the raw value.
 */
public class TimeCheckCondition implements LootCondition {
    /**
     * The immutable key for all {@code TimeCheckCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "time_check");

    private final @NotNull NumberRange range;
    private final @Nullable Number period;

    /**
     * Initialize a TimeCheckCondition with the provided range and an optional period, which runs the modulo operation
     * on the amount of time that the instance is at.
     */
    public TimeCheckCondition(@NotNull NumberRange range, @Nullable Number period){
        this.range = range;
        this.period = period;
    }

    /**
     * Returns the NumberRange that determines the range that this TimeCheckCondition can accept.
     */
    public @NotNull NumberRange range(){
        return range;
    }

    /**
     * Returns the Number that determines the amount of time that would be considered a 'day'. This runs the modulo
     * operation on the amount of time that the world has.
     */
    public @Nullable Number period(){
        return period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        if (this.period != null){
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
        if (instance == null){
            return false;
        }
        long value = instance.getTime();
        if (this.period != null){
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

    @Override
    public String toString() {
        return "TimeCheckCondition[range=" + range + ", period=" + period + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeCheckCondition that = (TimeCheckCondition) o;
        return range.equals(that.range) && Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return (range.hashCode() * 31) + Objects.hashCode(period);
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code TimeCheckCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        NumberRange range = JsonHelper.optionalAlternativeKey(json, loader::deserializeNumberRange, "range", "value");

        JsonElement number = json.get("period");
        Number period = null;
        if (number != null){
            period = JsonHelper.assureNumber(number, "period");
        }

        return new TimeCheckCondition(range, period);
    }
}

