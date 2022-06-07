package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonLootConverter;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the value of {@link #value()} is acceptable according to the
 * {@link NumberRange#predicate(LootContext, double)} method of {@link #range()}.
 */
public record ValueCheckCondition(@NotNull NumberProvider value, @NotNull NumberRange range) implements LootCondition {

    public static final @NotNull JsonLootConverter<ValueCheckCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:value_check"), ValueCheckCondition.class) {
        @Override
        public @NotNull ValueCheckCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new ValueCheckCondition(
                    loader.getNumberProviderManager().deserialize(json.get("value"), "value"),
                    NumberRange.deserialize(loader, json.get("range"), "range")
            );
        }

        @Override
        public void serialize(@NotNull ValueCheckCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            result.add("value", loader.getNumberProviderManager().serialize(input.value));
            result.add("range", input.range.serialize(loader));
        }
    };

    /**
     * Returns true if the value of {@link #value()} fits in the range of {@link #range()}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return this.range.predicate(context, this.value.getDouble(context));
    }
}
