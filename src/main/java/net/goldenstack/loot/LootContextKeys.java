package net.goldenstack.loot;

import io.leangen.geantyref.TypeToken;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

interface LootContextKeys {

    @NotNull LootContext.Key<Random> RANDOM = LootContext.key("minecraft:random", new TypeToken<>() {});

    @NotNull LootContext.Key<Float> EXPLOSION_RADIUS = LootContext.key("minecraft:explosion_radius", new TypeToken<>() {});
    
    @NotNull LootContext.Key<Player> LAST_DAMAGE_PLAYER = LootContext.key("minecraft:last_damage_player", new TypeToken<>() {});

    @NotNull LootContext.Key<Instance> WORLD = LootContext.key("minecraft:world", new TypeToken<>() {});

    @NotNull LootContext.Key<ItemStack> TOOL = LootContext.key("minecraft:tool", new TypeToken<>() {});

    @NotNull LootContext.Key<Map<NamespaceID, LootCondition>> REGISTERED_CONDITIONS = LootContext.key("minecraft:registered_loot_conditions", new TypeToken<>() {});

}
