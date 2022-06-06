package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code NumberProvider} that generates a value between the {@code min} and the {@code max}. The values
 * are uniformly generated (as suggested by the name).
 */
public record UniformNumber(@NotNull NumberProvider min, @NotNull NumberProvider max) implements NumberProvider {
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
     * For {@code UniformNumber}s, it's a uniform integer between the minimum's {@link NumberProvider#getInteger(LootContext)}
     * and the maximum's {@link NumberProvider#getInteger(LootContext)}.
     */
    @Override
    public int getInteger(@NotNull LootContext context) {
        final int max = this.max.getInteger(context), min = this.min.getInteger(context);
        return context.findRandom().nextInt(max - min + 1) + min;
    }

    public static final @NotNull JsonLootConverter<UniformNumber> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:uniform"), UniformNumber.class) {
        @Override
        public @NotNull UniformNumber deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new UniformNumber(
                    loader.getNumberProviderManager().deserialize(json.get("min"), "min"),
                    loader.getNumberProviderManager().deserialize(json.get("max"), "max")
            );
        }

        @Override
        public void serialize(@NotNull UniformNumber input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("min", loader.getNumberProviderManager().serialize(input.min));
            result.add("max", loader.getNumberProviderManager().serialize(input.max));
        }
    };
}