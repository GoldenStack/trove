package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonLootConverter;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if a random number from the provided LootContext is less than
 * {@code this.chance.getDouble(context) + context.looting() * this.lootingMultiplier.getDouble(context)}
 */
public record LootingRandomChanceCondition(@NotNull NumberProvider chance, @NotNull NumberProvider lootingMultiplier) implements LootCondition {

    /**
     * Returns true if {@code context.findRandom().nextDouble() < this.chance.getDouble(context) + context.looting() *
     * this.lootingMultiplier.getDouble(context);}
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return context.findRandom().nextDouble() < this.chance.getDouble(context) + context.looting() * this.lootingMultiplier.getDouble(context);
    }

    public static final @NotNull JsonLootConverter<LootingRandomChanceCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:random_chance_with_looting"), LootingRandomChanceCondition.class) {
        @Override
        public @NotNull LootingRandomChanceCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new LootingRandomChanceCondition(
                    loader.getNumberProviderManager().deserialize(json.get("chance"), "chance"),
                    loader.getNumberProviderManager().deserialize(json.get("looting_multiplier"), "looting_multiplier")
            );
        }

        @Override
        public void serialize(@NotNull LootingRandomChanceCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("chance", loader.getNumberProviderManager().serialize(input.chance));
            result.add("looting_multiplier", loader.getNumberProviderManager().serialize(input.lootingMultiplier));
        }
    };
}