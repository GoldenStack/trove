package dev.goldenstack.loot.condition;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a {@code LootCondition} that returns true if at least one of this instance's conditions is true according
 * to {@link LootCondition#or(LootContext, List)}.
 */
public class AlternativeCondition implements LootCondition {
    /**
     * The immutable key for all {@code AlternativeCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "alternative");

    private final ImmutableList<LootCondition> terms;

    /**
     * Initialize an AlternativeCondition with the provided LootConditions as terms
     */
    private AlternativeCondition(@NotNull ImmutableList<LootCondition> terms){
        this.terms = terms;
    }

    /**
     * Returns the list of LootConditions
     */
    public @NotNull ImmutableList<LootCondition> terms(){
        return terms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        object.add("terms", JsonHelper.serializeJsonArray(this.terms, loader.getLootConditionManager()::serialize));
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
     * Returns true if {@link LootCondition#or(LootContext, List)} returns true for {@link #terms()}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return LootCondition.or(context, this.terms);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return AlternativeCondition::deserialize;
    }

    @Override
    public String toString() {
        return "AlternativeCondition[terms=" + terms + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlternativeCondition that = (AlternativeCondition) o;
        return Objects.equals(terms, that.terms);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(terms) * 31;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code AlternativeCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return new AlternativeCondition(JsonHelper.deserializeJsonArray(json.get("terms"), "terms", loader.getLootConditionManager()::deserialize));
    }
}