package net.goldenstack.loot;

import io.leangen.geantyref.TypeToken;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

interface LootContextKeys {

    @NotNull LootContext.Key<Random> RANDOM = LootContext.key("minecraft:random", new TypeToken<>() {});

    @NotNull LootContext.Key<Float> EXPLOSION_RADIUS = LootContext.key("minecraft:explosion_radius", new TypeToken<>() {});
    
    @NotNull LootContext.Key<Player> LAST_DAMAGE_PLAYER = LootContext.key("minecraft:last_damage_player", new TypeToken<>() {});


}
