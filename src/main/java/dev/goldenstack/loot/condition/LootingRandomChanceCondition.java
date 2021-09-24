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
 * Represents a {@code LootCondition} that returns true if a random number from the provided LootContext is less than
 * {@code this.chance.getDouble(context) + context.looting() * this.lootingMultiplier.getDouble(context)}
 */
public class LootingRandomChanceCondition implements LootCondition {
    /**
     * The immutable key for all {@code LootingRandomChanceCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "random_chance_with_looting");

    private final NumberProvider chance, lootingMultiplier;

    /**
     * Initialize a LootingRandomChanceCondition with the provided probability of success and looting multiplier
     */
    public LootingRandomChanceCondition(@NotNull NumberProvider chance, @NotNull NumberProvider lootingMultiplier){
        this.chance = chance;
        this.lootingMultiplier = lootingMultiplier;
    }

    /**
     * Returns the number provider that calculates the chance of success
     */
    public @NotNull NumberProvider chance() {
        return chance;
    }

    /**
     * Returns the number provider that calculates the looting multiplier
     */
    public @NotNull NumberProvider lootingMultiplier() {
        return lootingMultiplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.add("chance", loader.getNumberProviderManager().serialize(chance));
        object.add("looting_multiplier", loader.getNumberProviderManager().serialize(lootingMultiplier));
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
     * Returns true if {@code context.findRandom().nextDouble() < this.chance.getDouble(context) + context.looting() *
     * this.lootingMultiplier.getDouble(context);}
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return context.findRandom().nextDouble() < this.chance.getDouble(context) + context.looting() * this.lootingMultiplier.getDouble(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return LootingRandomChanceCondition::deserialize;
    }

    @Override
    public String toString() {
        return "LootingRandomChanceCondition[chance-=" + chance + ", lootingMultiplier=" + lootingMultiplier + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootingRandomChanceCondition that = (LootingRandomChanceCondition) o;
        return Objects.equals(chance, that.chance) && Objects.equals(lootingMultiplier, that.lootingMultiplier);
    }

    @Override
    public int hashCode() {
        return (chance.hashCode() * 31 + lootingMultiplier.hashCode()) * 73;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code LootingRandomChanceCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new LootingRandomChanceCondition(
                loader.getNumberProviderManager().deserialize(json.get("chance"), "chance"),
                loader.getNumberProviderManager().deserialize(json.get("looting_multiplier"), "looting_multiplier")
        );
    }
}