package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a {@code LootCondition} that returns true if the value of {@link #value()} is acceptable according to the
 * {@link NumberRange#predicate(LootContext, double)} method of {@link #range()}.
 */
public class ValueCheckCondition implements LootCondition {
    /**
     * The immutable key for all {@code ValueCheckCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "value_check");

    private final NumberProvider value;
    private final NumberRange range;

    /**
     * Initialize a ValueCheckCondition with the provided NumberProvider and range of acceptable values
     */
    public ValueCheckCondition(@NotNull NumberProvider value, @NotNull NumberRange range){
        this.value = value;
        this.range = range;
    }

    /**
     * Returns the NumberProvider that is used to generate the value
     */
    public @NotNull NumberProvider value(){
        return value;
    }

    /**
     * Returns the NumberRange that is used to get the accepted range of values
     */
    public @NotNull NumberRange range(){
        return range;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.add("value", loader.getNumberProviderManager().serialize(value));
        object.add("range", loader.serializeNumberRange(range));
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
     * Returns true if the value of {@link #value()} fits in the range of {@link #range()}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return this.range.predicate(context, this.value.getDouble(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return ValueCheckCondition::deserialize;
    }

    @Override
    public String toString() {
        return "ValueCheckCondition[value=" + value + ", range=" + range + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueCheckCondition that = (ValueCheckCondition) o;
        return Objects.equals(value, that.value) && Objects.equals(range, that.range);
    }

    @Override
    public int hashCode() {
        return (value.hashCode() * 31 + range.hashCode()) * 37;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code ValueCheckCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new ValueCheckCondition(
                    loader.getNumberProviderManager().deserialize(json.get("value"), "value"),
                    NumberRange.deserialize(loader, json.get("range"), "range")
                );
    }
}
