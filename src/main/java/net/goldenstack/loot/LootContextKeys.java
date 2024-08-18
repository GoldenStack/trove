package net.goldenstack.loot;

import io.leangen.geantyref.TypeToken;
import net.goldenstack.loot.util.VanillaInterface;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Function;

interface LootContextKeys {

    @NotNull LootContext.Key<Random> RANDOM = LootContext.key("minecraft:random", new TypeToken<>() {});

    @NotNull LootContext.Key<Float> EXPLOSION_RADIUS = LootContext.key("minecraft:explosion_radius", new TypeToken<>() {});
    
    @NotNull LootContext.Key<Player> LAST_DAMAGE_PLAYER = LootContext.key("minecraft:last_damage_player", new TypeToken<>() {});

    @NotNull LootContext.Key<Instance> WORLD = LootContext.key("minecraft:world", new TypeToken<>() {});

    @NotNull LootContext.Key<ItemStack> TOOL = LootContext.key("minecraft:tool", new TypeToken<>() {});

    @NotNull LootContext.Key<Function<NamespaceID, LootPredicate>> REGISTERED_PREDICATES = LootContext.key("minecraft:registered_loot_predicates", new TypeToken<>() {});

    @NotNull LootContext.Key<Boolean> ENCHANTMENT_ACTIVE = LootContext.key("minecraft:enchantment_active", new TypeToken<>() {});
    
    @NotNull LootContext.Key<Block> BLOCK_STATE = LootContext.key("minecraft:block_state", new TypeToken<>() {});
    
    @NotNull LootContext.Key<DamageType> DAMAGE_SOURCE = LootContext.key("minecraft:damage_source", new TypeToken<>() {});

    @NotNull LootContext.Key<Point> ORIGIN = LootContext.key("minecraft:origin", new TypeToken<>() {});

    @NotNull LootContext.Key<Entity> DIRECT_ATTACKING_ENTITY = LootContext.key("minecraft:direct_attacking_entity", new TypeToken<>() {});

    @NotNull LootContext.Key<Entity> ATTACKING_ENTITY = LootContext.key("minecraft:attacking_entity", new TypeToken<>() {});

    @NotNull LootContext.Key<Entity> THIS_ENTITY = LootContext.key("minecraft:this_entity", new TypeToken<>() {});

    @NotNull LootContext.Key<VanillaInterface> VANILLA_INTERFACE = LootContext.key("trove:vanilla_interface", new TypeToken<>() {});

}
