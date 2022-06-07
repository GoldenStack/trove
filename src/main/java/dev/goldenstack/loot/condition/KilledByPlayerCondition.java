package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.json.JsonLootConverter;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootCondition} that returns true if the provided LootContext has a killer and the killer is a
 * player.
 */
public record KilledByPlayerCondition() implements LootCondition {

    public static final @NotNull JsonLootConverter<KilledByPlayerCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:killed_by_player"), KilledByPlayerCondition.class) {
        @Override
        public @NotNull KilledByPlayerCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new KilledByPlayerCondition();
        }

        @Override
        public void serialize(@NotNull KilledByPlayerCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            // Nothing!
        }
    };

    /**
     * Returns true if the context has a killer and the killer's EntityType is {@link EntityType#PLAYER}.
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        return context.assureParameter(LootContextParameter.KILLER_ENTITY).getEntityType() == EntityType.PLAYER;
    }
}