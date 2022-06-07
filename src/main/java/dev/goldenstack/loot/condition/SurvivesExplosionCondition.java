package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext generates a number less than
 * {@code 1 / radius}, where radius is the value of the {@link LootContextParameter#EXPLOSION_RADIUS} parameter. If
 * there is not a radius, it always returns true.
 */
public record SurvivesExplosionCondition() implements LootCondition {

    public static final @NotNull JsonLootConverter<SurvivesExplosionCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:survives_explosion"), SurvivesExplosionCondition.class) {
        @Override
        public @NotNull SurvivesExplosionCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new SurvivesExplosionCondition();
        }

        @Override
        public void serialize(@NotNull SurvivesExplosionCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            // Nothing!
        }
    };

    /**
     * Returns true if a random float from {@code context} is less than {@code 1 / radius}, where radius is the value
     * of the {@link LootContextParameter#EXPLOSION_RADIUS} parameter. If there is not a radius, it always returns true.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return context.findRandom().nextFloat() <= (1.0f / context.assureParameter(LootContextParameter.EXPLOSION_RADIUS));
    }
}