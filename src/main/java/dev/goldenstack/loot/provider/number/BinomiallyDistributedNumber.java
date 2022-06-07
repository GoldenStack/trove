package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Represents a {@code NumberProvider} that generates binomially distributed numbers based on the {@code trials} and
 * {@code probability}.
 */
public record BinomiallyDistributedNumber(@NotNull NumberProvider trials, @NotNull NumberProvider probability) implements NumberProvider {

    public static final @NotNull JsonLootConverter<BinomiallyDistributedNumber> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:binomial"), BinomiallyDistributedNumber.class) {
        @Override
        public @NotNull BinomiallyDistributedNumber deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new BinomiallyDistributedNumber(
                    JsonHelper.optionalAlternativeKey(json, loader.getNumberProviderManager()::deserialize, "trials", "n"),
                    JsonHelper.optionalAlternativeKey(json, loader.getNumberProviderManager()::deserialize, "probability", "p")
            );
        }

        @Override
        public void serialize(@NotNull BinomiallyDistributedNumber input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("trials", loader.getNumberProviderManager().serialize(input.trials));
            result.add("probability", loader.getNumberProviderManager().serialize(input.probability));
        }
    };
    
    /**
     * {@inheritDoc}<br>
     * Generates an integer via binomial distribution with {@link #trials} trials and {@link #probability} probability.
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        return getInteger(context);
    }

    /**
     * {@inheritDoc}<br>
     * Generates an integer via binomial distribution with {@link #trials} trials and {@link #probability} probability.
     */
    @Override
    public int getInteger(@NotNull LootContext context) {
        int trials = this.trials.getInteger(context);
        double probability = this.probability.getDouble(context);
        final Random random = context.findRandom();
        int val = 0;
        for (int i = 0; i < trials; i++) {
            if (random.nextDouble() < probability) {
                val++;
            }
        }
        return val;
    }
}