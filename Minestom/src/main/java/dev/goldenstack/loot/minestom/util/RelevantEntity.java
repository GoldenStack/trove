package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;

public enum RelevantEntity {
    THIS("this", LootContextKeys.THIS_ENTITY),
    KILLER("killer", LootContextKeys.KILLER_ENTITY),
    DIRECT_KILLER("direct_killer", LootContextKeys.DIRECT_KILLER_ENTITY),
    PLAYER_KILLER("killer_player", LootContextKeys.LAST_DAMAGE_PLAYER);

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

    public static @NotNull RelevantEntity from(@NotNull String id) throws ConfigurateException {
        for (var value : values()) {
            if (id.equals(value.id)) {
                return value;
            }
        }
        throw new ConfigurateException("Could not find a valid RelevantEntity key for the id \"" + id + "\"");
    }
}
