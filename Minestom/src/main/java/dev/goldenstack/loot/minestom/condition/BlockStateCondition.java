package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Checks that the {@link LootContextKeys#BLOCK_STATE} fits the information of this condition.
 * @param blockKey the required NamespaceID of the block
 * @param check the {@link BlockStateCheck} that must pass
 */
public record BlockStateCondition(@NotNull NamespaceID blockKey, @NotNull BlockStateCheck check) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for block state conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, BlockStateCondition> CONVERTER = Utils.createKeyedConverter("minecraft:block_state_property", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("block").set(input.blockKey.asString());
                result.node("properties").set(BlockStateCheck.CONVERTER.serialize(input.check, context));
            }, (input, context) -> new BlockStateCondition(
                    NamespaceID.from(input.node("block").require(String.class)),
                    BlockStateCheck.CONVERTER.deserialize(input.node("properties"), context)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var block = context.assure(LootContextKeys.BLOCK_STATE);
        return block.namespace().equals(blockKey) && check.verify(block);
    }
}
