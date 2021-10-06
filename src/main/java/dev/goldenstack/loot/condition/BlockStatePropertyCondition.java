package dev.goldenstack.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.criterion.PropertiesCriterion;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a {@code LootCondition} that returns true if:
 * <ul>
 *     <li>{@link #block()} is null or the context's block's NamespaceID is equal to {@link #block()} and</li>
 *     <li>There are no properties or all provided properties apply to the provided block</li>
 * </ul>
 */
public class BlockStatePropertyCondition implements LootCondition {
    /**
     * The immutable key for all {@code BlockStatePropertyCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "entity_properties");

    private final @Nullable NamespaceID block;
    private final PropertiesCriterion properties;

    /**
     * Initialize a BlockStatePropertyCondition with the provided PropertiesCriterion
     */
    public BlockStatePropertyCondition(@Nullable NamespaceID block, @NotNull PropertiesCriterion properties){
        this.block = block;
        this.properties = properties;
    }

    /**
     * Returns the NamespaceID that represents the block. This can be null, meaning that any block works.
     */
    public @Nullable NamespaceID block(){
        return block;
    }

    /**
     * Returns the PropertiesCriterion that is used to test for properties
     */
    public @NotNull PropertiesCriterion properties(){
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        if (this.block != null) {
            object.addProperty("block", this.block.asString());
        }
        object.add("properties", this.properties.serialize());
    }

    /**
     * {@inheritDoc}
     * @return {@link #KEY}
     */
    @Override
    public @NotNull NamespaceID getKey() {
        return KEY;
    }

    /**
     * Returns true if:
     * <ul>
     *     <li>{@link #block()} is null or the context's block's NamespaceID is equal to {@link #block()} and</li>
     *     <li>There are no properties or all provided properties apply to the provided block</li>
     * </ul>
     */
    @Override
    public boolean test(@NotNull LootContext context) {
        Block block = context.assureParameter(LootContextParameter.BLOCK_STATE);
        if (this.block != null && !this.block.equals(block.registry().namespace())){
            return false;
        }
        return this.properties.test(block.properties());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootCondition>> getDeserializer() {
        return BlockStatePropertyCondition::deserialize;
    }

    @Override
    public String toString() {
        return "BlockStatePropertyCondition[block=" + block + ", properties=" + properties + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockStatePropertyCondition that = (BlockStatePropertyCondition) o;
        return Objects.equals(block, that.block) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(block) * 31 + Objects.hashCode(properties);
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code BlockStatePropertyCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        JsonElement blockElement = json.get("block");
        NamespaceID id = null;
        if (!JsonHelper.isNull(blockElement)){
            id = JsonHelper.assureNamespaceId(blockElement, "block");
        }
        return new BlockStatePropertyCondition(id, PropertiesCriterion.deserialize(json.get("properties")));
    }
}