package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents some arbitrary entity that is relevant to the loot drop.
 */
public enum RelevantEntity {
    /**
     * Refers to the source of this loot drop; this should be the entity that got killed.
     */
    THIS("this", LootContextKeys.THIS_ENTITY),

    /**
     * Refers to the entity that killed the source, possibly indirectly; for example, if a player shot an arrow and
     * killed the source, this would refer to the player that shot the arrow. If it's not as complicated as that, for
     * example, in a situation of simple melee damage, it should just be the actual killer.
     */
    KILLER("killer", LootContextKeys.KILLER_ENTITY),

    /**
     * Refers to the actual entity that killed the source; for example, if a player shot an arrow and killed the source,
     * this would refer to the arrow that dealt the lethal damage. If it's not as complicated as that, for example in a
     * situation of simple melee damage, it should just be the actual killer.
     */
    DIRECT_KILLER("direct_killer", LootContextKeys.DIRECT_KILLER_ENTITY),

    /**
     * Refers to the last player that dealt damage to the source.
     */
    LAST_PLAYER_DAMAGE("killer_player", LootContextKeys.LAST_DAMAGE_PLAYER);

    private final @NotNull String id;
    private final @NotNull LootContext.Key<? extends Entity> key;

    RelevantEntity(@NotNull String id, @NotNull LootContext.Key<? extends Entity> key) {
        this.id = id;
        this.key = key;
    }

    /**
     * Retrieves the RelevantEntity with the {@link #id()} equal to the provided one.
     * @param id the id to check
     * @return the RelevantEntity representing the id, or null if there is not one
     */
    public static @Nullable RelevantEntity ofId(@NotNull String id) {
        for (var value : values()) {
            if (value.id().equals(id)) {
                return value;
            }
        }
        return null;
    }

    /**
     * @return the string-based identifier of this relevant entity
     */
    public @NotNull String id() {
        return id;
    }

    /**
     * @return the loot context key referring to the relevant entity
     */
    public @NotNull LootContext.Key<? extends Entity> key() {
        return key;
    }

}
