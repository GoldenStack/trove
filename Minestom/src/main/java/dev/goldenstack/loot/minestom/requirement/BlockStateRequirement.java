package dev.goldenstack.loot.minestom.requirement;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.KeyedLootConverter;
import dev.goldenstack.loot.minestom.check.BlockStateCheck;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.Converters;
import dev.goldenstack.loot.structure.LootRequirement;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Assures that the block of each provided context matches {@link #block()} and that it passes {@link #check()}.
 * @param block the block to compare
 * @param check the block property check
 */
public record BlockStateRequirement(@NotNull Block block, @NotNull BlockStateCheck check) implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, BlockStateRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:block_state_property", TypeToken.get(BlockStateRequirement.class)) {
        @Override
        public @NotNull BlockStateRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new BlockStateRequirement(
                    Converters.BLOCK_CONVERTER.deserialize(node.node("block"), context),
                    BlockStateCheck.deserialize(node.node("properties"), context)
            );
        }

        @Override
        public void serialize(@NotNull BlockStateRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("block").set(Converters.BLOCK_CONVERTER.serialize(input.block(), context));
            result.node("properties").set(BlockStateCheck.serialize(input.check(), context));
        }
    };

    /**
     * @param context the loot context that will be verified
     * @return true if the block from the context was equal to {@link #block()} and it passed {@link #check()}
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        Block contextBlock = context.assure(LootContextKeys.BLOCK_STATE);
        if (!block.namespace().equals(contextBlock.namespace())) {
            return false;
        }
        return check.test(contextBlock);
    }
}
