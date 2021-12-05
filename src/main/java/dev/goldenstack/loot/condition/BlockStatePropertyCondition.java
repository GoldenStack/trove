package dev.goldenstack.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
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

/**
 * Represents a {@code LootCondition} that returns true if:
 * <ul>
 *     <li>{@link #block()} is null or the context's block's NamespaceID is equal to {@link #block()} and</li>
 *     <li>There are no properties or all provided properties apply to the provided block</li>
 * </ul>
 */
public record BlockStatePropertyCondition(@Nullable NamespaceID block, @NotNull PropertiesCriterion properties) implements LootCondition {

    /**
     * The immutable key for all {@code BlockStatePropertyCondition}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "block_state_property");

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
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
        if (this.block != null && !this.block.equals(block.registry().namespace())) {
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

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code BlockStatePropertyCondition}
     */
    public static @NotNull LootCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        JsonElement blockElement = json.get("block");
        NamespaceID id = null;
        if (!JsonHelper.isNull(blockElement)) {
            id = JsonHelper.assureNamespaceId(blockElement, "block");
        }
        return new BlockStatePropertyCondition(id, PropertiesCriterion.deserialize(json.get("properties")));
    }
}