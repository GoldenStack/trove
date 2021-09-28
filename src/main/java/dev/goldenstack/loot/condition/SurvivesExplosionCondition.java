package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext generates a number less than
 * {@code 1 / radius}, where radius is the value of the {@link LootContextParameter#EXPLOSION_RADIUS} parameter. If
 * there is not a radius, it always returns true.
 */
public class SurvivesExplosionCondition implements LootCondition {
    /**
     * The immutable key for all {@code SurvivesExplosionCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "survives_explosion");

    /**
     * The single instance of {@code SurvivesExplosionCondition}. Since there are no instance variables, this shouldn't
     * break anything.
     */
    public static final @NotNull SurvivesExplosionCondition INSTANCE = new SurvivesExplosionCondition();

    private SurvivesExplosionCondition(){}

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {}

    /**
     * {@inheritDoc}
     * @return {@link #KEY}
     */
    @Override
    public @NotNull NamespaceID getKey() {
        return KEY;
    }

    /**
     * Returns true if a random float from {@code context} is less than {@code 1 / radius}, where radius is the value
     * of the {@link LootContextParameter#EXPLOSION_RADIUS} parameter. If there is not a radius, it always returns true.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        Float radius = context.getParameter(LootContextParameter.EXPLOSION_RADIUS);
        if (radius != null) {
            return context.findRandom().nextFloat() <= (1.0f / radius);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return SurvivesExplosionCondition::deserialize;
    }

    @Override
    public String toString() {
        return "SurvivesExplosionCondition[]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SurvivesExplosionCondition;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code SurvivesExplosionCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return SurvivesExplosionCondition.INSTANCE;
    }
}