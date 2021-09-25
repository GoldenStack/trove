package dev.goldenstack.loot.context;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.html.parser.Entity;
import java.util.Objects;

/**
 * Represents information about something that happened in a LootContext
 */
public class LootContextParameter <T> {

    /**
     * Represents the entity that is getting loot generated for it
     */
    public static final LootContextParameter<Entity> THIS_ENTITY = new LootContextParameter<>("this_entity");

    /**
     * Represents the last damage that was dealt to the entity
     */
    public static final LootContextParameter<Player> LAST_DAMAGE_PLAYER = new LootContextParameter<>("last_damage_player");

    /**
     * Represents the source of the damage
     */
    public static final LootContextParameter<DamageType> DAMAGE_SOURCE = new LootContextParameter<>("damage_source");

    /**
     * Represents the entity that killed something. If a player shoots an arrow and kills something, this will represent
     * the player.
     */
    public static final LootContextParameter<Entity> KILLER_ENTITY = new LootContextParameter<>("killer_entity");

    /**
     * Represents the exact entity that killed something. If a player shoots an arrow and kills something, this will
     * represent the arrow.
     */
    public static final LootContextParameter<Entity> DIRECT_KILLER_ENTITY = new LootContextParameter<>("direct_killer_entity");

    /**
     * Represents the origin of whatever happened
     */
    public static final LootContextParameter<Pos> ORIGIN = new LootContextParameter<>("origin");

    /**
     * Represents the block state
     */
    public static final LootContextParameter<Block> BLOCK_STATE = new LootContextParameter<>("block_state");

    /**
     * Represents the block entity. There is not currently a way to implement this in Minestom, so the generic is unknown.
     */
    public static final LootContextParameter<? /* TileEntity */> BLOCK_ENTITY = new LootContextParameter<>("block_entity");

    /**
     * Represents the tool that was used in the event
     */
    public static final LootContextParameter<ItemStack> TOOL = new LootContextParameter<>("tool");

    /**
     * Represents the explosion radius of an explosion that occurred
     */
    public static final LootContextParameter<Float> EXPLOSION_RADIUS = new LootContextParameter<>("explosion_radius");

    private final NamespaceID key;

    /**
     * Creates a new LootContextParameter with the provided key
     */
    public LootContextParameter(@NotNull NamespaceID key){
        this.key = key;
    }

    /**
     * Creates a new LootContextParameter with the result of {@code NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, key)}
     * , where {@code key} is the {@code key} parameter.
     */
    public LootContextParameter(@NotNull String key){
        this.key = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, key);
    }

    /**
     * Returns this LootContextParameter's key
     */
    public @NotNull NamespaceID key(){
        return key;
    }

    @Override
    public String toString() {
        return "LootContextParameter[key=" + key + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootContextParameter<?> that = (LootContextParameter<?>) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
