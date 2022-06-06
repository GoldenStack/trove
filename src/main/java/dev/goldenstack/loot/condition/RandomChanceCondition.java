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
 * Represents a {@code LootCondition} that returns true if the provided LootContext generates a number less than the
 * value of this instance's {@code probability} field.
 */
public record RandomChanceCondition(@NotNull NumberProvider probability) implements LootCondition {

    /**
     * Returns true if {@code context.findRandom().nextDouble() < this.probability.getDouble(context);}
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return context.findRandom().nextDouble() < this.probability.getDouble(context);
    }

    public static final @NotNull JsonLootConverter<RandomChanceCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:random_chance"), RandomChanceCondition.class) {
        @Override
        public @NotNull RandomChanceCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new RandomChanceCondition(loader.getNumberProviderManager().deserialize(json.get("probability"), "probability"));
        }

        @Override
        public void serialize(@NotNull RandomChanceCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("probability", loader.getNumberProviderManager().serialize(input.probability));
        }
    };

}