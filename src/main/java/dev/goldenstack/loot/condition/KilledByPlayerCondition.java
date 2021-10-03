package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext has a killer and the killer is a
 * player.
 */
public class KilledByPlayerCondition implements LootCondition {
    /**
     * Since this implementation doesn't have any settings, it can be stored in a single instance.
     */
    public static final @NotNull KilledByPlayerCondition INSTANCE = new KilledByPlayerCondition();

    /**
     * The immutable key for all {@code KilledByPlayerCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "killed_by_player");

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        // Nothing!
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
     * Returns true if the context has a killer and the killer's EntityType is {@link EntityType#PLAYER}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        Entity entity = context.getParameter(LootContextParameter.KILLER_ENTITY);
        if (entity == null){
            return false;
        }
        return entity.getEntityType() == EntityType.PLAYER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return KilledByPlayerCondition::deserialize;
    }

    @Override
    public String toString() {
        return "KilledByPlayerCondition[]";
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KilledByPlayerCondition;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code KilledByPlayerCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        return KilledByPlayerCondition.INSTANCE;
    }
}