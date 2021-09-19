package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code NumberProvider} that is a constant value.
 */
public class ConstantNumber implements NumberProvider {
    /**
     * The immutable key for all {@code ConstantNumber}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "constant");

    private final double value;

    /**
     * Initialize a ConstantNumber with the provided value.
     */
    public ConstantNumber(double value){
        this.value = value;
    }

    /**
     * Returns the value that this instance contains
     */
    public double value(){
        return value;
    }

    /**
     * {@inheritDoc}<br>
     * For {@code ConstantNumber}s, the value should never change.
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.addProperty("value", this.value);
    }

    /**
     * {@inheritDoc}
     * @return {@link #KEY}
     */
    @Override
    public @NotNull NamespaceID getKey() {
        return KEY;
    }

    @Override
    public String toString() {
        return "ConstantNumber[value=" + value + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Double.compare(((ConstantNumber) o).value, value) == 0;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code ConstantNumber}
     */
    public static @NotNull NumberProvider deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new ConstantNumber(JsonHelper.getNumber(json.get("value"), "value").doubleValue());
    }
}