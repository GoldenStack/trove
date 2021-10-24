package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code NumberProvider} that generates a value between the {@code min} and the {@code max}. The values
 * are uniformly generated (as suggested by the name).
 */
public record UniformNumber(@NotNull NumberProvider min, @NotNull NumberProvider max) implements NumberProvider {
    /**
     * The immutable key for all {@code UniformNumber}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "uniform");

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
    public @NotNull LootDeserializer<? extends LootSerializer<NumberProvider>> getDeserializer() {
        return UniformNumber::deserialize;
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