package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * Checks that the {@link LootContextKeys#BLOCK_STATE} fits the information of this condition.
 * @param blockKey the required NamespaceID of the block
 * @param check the {@link BlockStateCheck} that must pass
 */
public record BlockStateCondition(@NotNull NamespaceID blockKey, @NotNull BlockStateCheck check) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:block_state_property";

    /**
     * A standard map-based converter for block state conditions.
     */
    public static final @NotNull TypeSerializer<BlockStateCondition> CONVERTER =
            converter(BlockStateCondition.class,
                    field(NamespaceID.class).name("blockKey").nodePath("block"),
                    field(BlockStateCheck.class).name("check").nodePath("properties")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var block = context.assure(LootContextKeys.BLOCK_STATE);
        return block.namespace().equals(blockKey) && check.verify(block);
    }
}
