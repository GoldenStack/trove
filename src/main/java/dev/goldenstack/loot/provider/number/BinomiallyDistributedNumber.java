package dev.goldenstack.loot.provider.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonDeserializable;
import dev.goldenstack.loot.json.JsonSerializable;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static dev.goldenstack.loot.json.JsonHelper.*;

/**
 * Represents a {@code NumberProvider} that generates binomially distributed numbers based on the {@code trials} and
 * {@code probability}.
 */
public class BinomiallyDistributedNumber implements NumberProvider {
    /**
     * The immutable key for all {@code BinomiallyDistributedNumber}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "binomial");

    private final @NotNull NumberProvider trials, probability;

    /**
     * Initializes a {@code BinomiallyDistributedNumber} with the provided trials and probability providers.
     */
    public BinomiallyDistributedNumber(@NotNull NumberProvider trials, @NotNull NumberProvider probability){
        this.trials = trials;
        this.probability = probability;
    }

    /**
     * Returns the value that this instance uses to calculate the number of trials
     */
    public @NotNull NumberProvider trials(){
        return this.trials;
    }

    /**
     * Returns the value that this instance uses to calculate the probability of success
     */
    public @NotNull NumberProvider probability(){
        return this.probability;
    }

    /**
     * {@inheritDoc}<br>
     * Generates an integer via binomial distribution with {@link #trials} trials and {@link #probability} probability.
     */
    @Override
    public double getDouble(@NotNull LootContext context) {
        return getInt(context);
    }

    /**
     * {@inheritDoc}<br>
     * Generates an integer via binomial distribution with {@link #trials} trials and {@link #probability} probability.
     */
    @Override
    public int getInt(@NotNull LootContext context) {
        int trials = this.trials.getInt(context);
        double probability = this.probability.getDouble(context);
        final Random random = context.findRandom();
        int val = 0;
        for (int i = 0; i < trials; i++){
            if (random.nextDouble() < probability){
                val++;
            }
        }
        return val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.add("trials", loader.getNumberProviderManager().serialize(this.trials));
        object.add("probability", loader.getNumberProviderManager().serialize(this.probability));
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
    public @NotNull JsonDeserializable<? extends JsonSerializable<NumberProvider>> getDeserializer() {
        return BinomiallyDistributedNumber::deserialize;
    }

    @Override
    public String toString() {
        return "BinomiallyDistributedNumber[trials=" + trials + ", probability=" + probability + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinomiallyDistributedNumber that = (BinomiallyDistributedNumber) o;
        return trials.equals(that.trials) && probability.equals(that.probability);
    }

    @Override
    public int hashCode() {
        return (trials.hashCode() * 31 + probability.hashCode()) * 47;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code BinomiallyDistributedNumber}
     */
    public static @NotNull NumberProvider deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        NumberProvider trials, probability;
        // Try keys "trials" and "probability" first, then try the deprecated "n" and "p" that default Minecraft uses.

        // Look for the trials key, if it doesn't exist, look for the "n" key. If neither exist, fallback with key "trials"
        JsonElement rawTrials = json.get("trials");
        if (!isNull(rawTrials)){
            trials = loader.getNumberProviderManager().deserialize(rawTrials, "trials");
        } else {
            JsonElement rawN = json.get("n");
            if (isNull(rawN)){
                // This will call the default deserializer 100% of the time
                trials = loader.getNumberProviderManager().deserialize(rawTrials, "trials");
            } else {
                trials = loader.getNumberProviderManager().deserialize(rawN, "n");
            }
        }

        JsonElement rawProbability = json.get("probability");
        if (!isNull(rawProbability)) {
            probability = loader.getNumberProviderManager().deserialize(rawProbability, "probability");
        } else {
            JsonElement rawP = json.get("p");
            if (isNull(rawP)){
                // This will call the default deserializer 100% of the time
                probability = loader.getNumberProviderManager().deserialize(rawProbability, "probability");
            } else {
                probability = loader.getNumberProviderManager().deserialize(rawP, "p");
            }
        }
        return new BinomiallyDistributedNumber(trials, probability);
    }
}