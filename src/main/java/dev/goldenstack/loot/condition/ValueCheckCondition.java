package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the value of {@link #value()} is acceptable according to the
 * {@link NumberRange#predicate(LootContext, double)} method of {@link #range()}.
 */
public record ValueCheckCondition(@NotNull NumberProvider value, @NotNull NumberRange range) implements LootCondition {
    /**
     * The immutable key for all {@code ValueCheckCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "value_check");

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.add("value", loader.getNumberProviderManager().serialize(value));
        object.add("range", loader.serializeNumberRange(range));
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
     * Returns true if the value of {@link #value()} fits in the range of {@link #range()}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return this.range.predicate(context, this.value.getDouble(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return ValueCheckCondition::deserialize;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code ValueCheckCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new ValueCheckCondition(
                loader.getNumberProviderManager().deserialize(json.get("value"), "value"),
                NumberRange.deserialize(loader, json.get("range"), "range")
        );
    }
}
