package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a {@code NumberProvider} that generates a value between the {@code min} and the {@code max}. The values
 * are uniformly generated (as suggested by the name).
 */
public class UniformNumber implements NumberProvider {
    /**
     * The immutable key for all {@code UniformNumber}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "uniform");

    private final NumberProvider min, max;

    /**
     * Initialize a UniformNumber with the provided minimum and maximum values.
     */
    public UniformNumber(@NotNull NumberProvider min, @NotNull NumberProvider max){
        this.min = min;
        this.max = max;
    }

    /**
     * {@inheritDoc}<br>
     * For {@code UniformNumber}s, it's a uniform value between the minimum and the maximum.
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        double min = this.min.getDouble(context), max = this.max.getDouble(context);
        return context.findRandom().nextDouble() * (max - min) + min;
    }

    /**
     * {@inheritDoc}<br>
     * For {@code UniformNumber}s, it's a uniform integer between the minimum's {@link NumberProvider#getInt(LootContext)}
     * and the maximum's {@link NumberProvider#getInt(LootContext)}.
     */
    @Override
    public int getInt(@NotNull LootContext context) {
        final int max = this.max.getInt(context), min = this.min.getInt(context);
        return context.findRandom().nextInt(max - min) + min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.add("min", loader.getNumberProviderManager().serialize(this.min));
        object.add("max", loader.getNumberProviderManager().serialize(this.max));
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
        return "UniformNumber[min=" + min + ", max=" + max +"]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniformNumber that = (UniformNumber) o;
        return Objects.equals(min, that.min) && Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return min.hashCode() * 31 + max.hashCode();
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code UniformNumber}
     */
    public static @NotNull NumberProvider deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new UniformNumber(
                loader.getNumberProviderManager().deserialize(json.get("min"), "min"),
                loader.getNumberProviderManager().deserialize(json.get("max"), "max")
        );
    }
}