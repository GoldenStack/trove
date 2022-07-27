package dev.goldenstack.loot.minestom.context;

import dev.goldenstack.loot.context.LootContext;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Contains all loot context keys that are commonly used in this library.
 */
public class LootContextKeys {
    private LootContextKeys() {}

    /**
     * Represents the block entity (block with NBT) that is related to a loot context. Importantly, in Minestom, all
     * blocks may have NBT, so this might have slightly different behaviour.
     */
    public static final @NotNull LootContext.Key<Block> BLOCK_ENTITY = key("minecraft:block_entity", Block.class);

    /**
     * Represents the block state that is related to a loot context.
     */
    public static final @NotNull LootContext.Key<Block> BLOCK_STATE = key("minecraft:block_state", Block.class);

    /**
     * Represents the source of the damage that, directly or indirectly, caused the source event.
     */
    public static final @NotNull LootContext.Key<DamageType> DAMAGE_SOURCE = key("minecraft:damage_source", DamageType.class);

    /**
     * Represents the amount of luck that the context has. For loot tables, this is generally used to give better items,
     * of course being based on what the loot table itself considers good.
     */
    public static final @NotNull LootContext.Key<Double> LUCK = key("minecraft:luck", Double.class);

    /**
     * Represents the direct killer of whatever created this loot context. For example, this is the arrow that killed a
     * mob, {@link #KILLER_ENTITY} would be the entity that shot the arrow.
     */
    public static final @NotNull LootContext.Key<Entity> DIRECT_KILLER_ENTITY = key("minecraft:direct_killer_entity", Entity.class);

    /**
     * Represents the explosion radius of whatever, directly or indirectly, caused the source event. For example, if a
     * block is exploded by TNT, and it dropped this loot table, this property would be the TNT's explosion radius.
     */
    public static final @NotNull LootContext.Key<Float> EXPLOSION_RADIUS = key("minecraft:explosion_radius", Float.class);

    /**
     * Represents the world that the source event occurred in.
     */
    public static final @NotNull LootContext.Key<Instance> WORLD = key("minecraft:world", Instance.class);

    /**
     * Represents the actual killer of whatever caused this source event. For example, if {@link #DIRECT_KILLER_ENTITY}
     * is the arrow that killed the mob, this would be the source of the arrow.
     */
    public static final @NotNull LootContext.Key<Entity> KILLER_ENTITY = key("minecraft:killer_entity", Entity.class);

    /**
     * Represents the last player that dealt damage to whatever caused the source event.
     */
    public static final @NotNull LootContext.Key<Player> LAST_DAMAGE_PLAYER = key("minecraft:last_damage_player", Player.class);

    /**
     * Represents the origin point of the source event. For example, this could be the location of the block that was
     * mined, the chest that was opened, or the entity that was killed.
     */
    public static final @NotNull LootContext.Key<Pos> ORIGIN = key("minecraft:origin", Pos.class);

    /**
     * Represents the entity, if it exists, that was the source of the event. For example, if a mob is killed, this
     * would be the mob.
     */
    public static final @NotNull LootContext.Key<Entity> THIS_ENTITY = key("minecraft:this_entity", Entity.class);

    /**
     * Represents the tool that, directly or indirectly, caused the source event. For example, if a player mines a block
     * with a tool, this key would hold the tool.
     */
    public static final @NotNull LootContext.Key<ItemStack> TOOL = key("minecraft:tool", ItemStack.class);

    private static <T> LootContext.Key<T> key(@NotNull String key, @NotNull Class<T> type) {
        return new LootContext.Key<>(key, TypeToken.get(type));
    }
}
