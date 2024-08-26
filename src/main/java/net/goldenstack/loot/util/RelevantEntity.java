package net.goldenstack.loot.util;

import net.goldenstack.loot.LootContext;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public enum RelevantEntity {
    THIS("this", LootContext.THIS_ENTITY),
    KILLER("killer", LootContext.ATTACKING_ENTITY),
    DIRECT_KILLER("direct_killer", LootContext.DIRECT_ATTACKING_ENTITY),
    LAST_PLAYER_DAMAGE("killer_player", LootContext.LAST_DAMAGE_PLAYER);

    private final @NotNull String id;
    private final @NotNull LootContext.Key<? extends Entity> key;

    RelevantEntity(@NotNull String id, @NotNull LootContext.Key<? extends Entity> key) {
        this.id = id;
        this.key = key;
    }

    public @NotNull String id() {
        return id;
    }

    public @NotNull LootContext.Key<? extends Entity> key() {
        return key;
    }

}
