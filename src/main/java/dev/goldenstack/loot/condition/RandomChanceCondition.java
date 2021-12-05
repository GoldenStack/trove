package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext generates a number less than the
 * value of this instance's {@code probability} field.
 */
public record RandomChanceCondition(@NotNull NumberProvider probability) implements LootCondition {

    /**
     * The immutable key for all {@code RandomChanceCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "random_chance");

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
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

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code RandomChanceCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        return new RandomChanceCondition(loader.getNumberProviderManager().deserialize(json.get("probability"), "probability"));
    }
}