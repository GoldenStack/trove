package dev.goldenstack.loot.minestom.context;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.vanilla.VanillaInterface;
import dev.goldenstack.loot.structure.LootCondition;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Contains all loot context keys that are commonly used in this library.
 */
public class LootContextKeys {
    private LootContextKeys() {}

    /**
     * Represents the block state that is related to a loot context.
     */
    public static final @NotNull LootContext.Key<Block> BLOCK_STATE = key("minecraft:block_state", new TypeToken<>(){});

    /**
     * Represents the source of the damage that, directly or indirectly, caused the source event.
     */
    public static final @NotNull LootContext.Key<DamageType> DAMAGE_SOURCE = key("minecraft:damage_source", new TypeToken<>(){});

    /**
     * Represents the amount of luck that the context has. For loot tables, this is generally used to give better items,
     * of course being based on what the loot table itself considers good.
     */
    public static final @NotNull LootContext.Key<Double> LUCK = key("minecraft:luck", new TypeToken<>(){});

    /**
     * Represents the direct killer of whatever created this loot context. For example, this is the arrow that killed a
     * mob; {@link #KILLER_ENTITY} would be the entity that shot the arrow.
     */
    public static final @NotNull LootContext.Key<Entity> DIRECT_KILLER_ENTITY = key("minecraft:direct_killer_entity", new TypeToken<>(){});

    /**
     * Represents the explosion radius of whatever, directly or indirectly, caused the source event. For example, if a
     * block is exploded by TNT, and it dropped this loot table, this property would be the TNT's explosion radius.
     */
    public static final @NotNull LootContext.Key<Float> EXPLOSION_RADIUS = key("minecraft:explosion_radius", new TypeToken<>(){});

    /**
     * Represents the world that the source event occurred in.
     */
    public static final @NotNull LootContext.Key<Instance> WORLD = key("minecraft:world", new TypeToken<>(){});

    /**
     * Represents the actual killer of whatever caused this source event. For example, if {@link #DIRECT_KILLER_ENTITY}
     * is the arrow that killed the mob, this would be the source of the arrow.
     */
    public static final @NotNull LootContext.Key<Entity> KILLER_ENTITY = key("minecraft:killer_entity", new TypeToken<>(){});

    /**
     * Represents the last player that dealt damage to whatever caused the source event.
     */
    public static final @NotNull LootContext.Key<Player> LAST_DAMAGE_PLAYER = key("minecraft:last_damage_player", new TypeToken<>(){});

    /**
     * Represents the origin point of the source event. For example, this could be the location of the block that was
     * mined, the chest that was opened, or the entity that was killed.
     */
    public static final @NotNull LootContext.Key<Point> ORIGIN = key("minecraft:origin", new TypeToken<>(){});

    /**
     * Represents the entity, if it exists, that was the source of the event. For example, if a mob is killed, this
     * would be the mob.
     */
    public static final @NotNull LootContext.Key<Entity> THIS_ENTITY = key("minecraft:this_entity", new TypeToken<>(){});

    /**
     * Represents the tool that, directly or indirectly, caused the source event. For example, if a player mines a block
     * with a tool, this key would hold the tool.
     */
    public static final @NotNull LootContext.Key<ItemStack> TOOL = key("minecraft:tool", new TypeToken<>(){});

    /**
     * Represents an instance of {@link VanillaInterface}, which is meant to provide, well, an interface to vanilla
     * features without forcing any specific implementation.
     */
    public static final @NotNull LootContext.Key<VanillaInterface> VANILLA_INTERFACE = key("minecraft:vanilla_interface", new TypeToken<>(){});

    /**
     * Represents a map of all registered loot tables.
     */
    public static final @NotNull LootContext.Key<Map<NamespaceID, LootGenerator>> REGISTERED_TABLES = key("minecraft:registered_loot_tables", new TypeToken<>(){});

    /**
     * Represents a map of all registered loot conditions.
     */
    public static final @NotNull LootContext.Key<Map<NamespaceID, LootCondition>> REGISTERED_CONDITIONS = key("minecraft:registered_loot_conditions", new TypeToken<>(){});

    @Contract("_, _ -> new")
    private static <T> LootContext.@NotNull Key<T> key(@NotNull String key, @NotNull TypeToken<T> typeToken) {
        return new LootContext.Key<>(key, typeToken);
    }
}
