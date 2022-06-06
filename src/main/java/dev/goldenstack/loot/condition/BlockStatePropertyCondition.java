package dev.goldenstack.loot.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.criterion.PropertiesCriterion;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
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

    public static final @NotNull JsonLootConverter<BlockStatePropertyCondition> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:block_state_property"), BlockStatePropertyCondition.class) {
        @Override
        public @NotNull BlockStatePropertyCondition deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new BlockStatePropertyCondition(
                    JsonHelper.getAsNamespaceId(json.get("block")),
                    PropertiesCriterion.deserialize(json.get("properties"))
            );
        }

        @Override
        public void serialize(@NotNull BlockStatePropertyCondition input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            if (input.block != null) {
                result.addProperty("block", input.block.asString());
            }
            result.add("properties", input.properties.serialize());
        }
    };
}