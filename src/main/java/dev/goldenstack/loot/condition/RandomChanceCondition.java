package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext generates a number less than the
 * value of this instance's {@code probability} field.
 */
public class RandomChanceCondition implements LootCondition {
    /**
     * The immutable key for all {@code RandomChanceCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "random_chance");

    private final NumberProvider probability;

    /**
     * Initialize a RandomChanceCondition with the provided probability of success
     */
    public RandomChanceCondition(@NotNull NumberProvider probability){
        this.probability = probability;
    }

    /**
     * Returns the number provider that calculates the probability of being true
     */
    public @NotNull NumberProvider probability(){
        return probability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
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

    /**
     * Returns true if {@code context.findRandom().nextDouble() < this.probability.getDouble(context);}
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return context.findRandom().nextDouble() < this.probability.getDouble(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return RandomChanceCondition::deserialize;
    }

    public String toString(){
        return "RandomChanceCondition[probability=" + probability + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomChanceCondition that = (RandomChanceCondition) o;
        return Objects.equals(probability, that.probability);
    }

    @Override
    public int hashCode() {
        return probability.hashCode() * 59;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code RandomChanceCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new RandomChanceCondition(loader.getNumberProviderManager().deserialize(json.get("probability"), "probability"));
    }
}