package dev.goldenstack.loot.minestom.requirement;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.conversion.LootConverter;
import dev.goldenstack.loot.minestom.check.BlockStateCheck;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootRequirement;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Assures that the block of each provided context matches {@link #block()} and that it passes {@link #check()}.
 * @param block the block to compare
 * @param check the block property check
 */
public record BlockStateRequirement(@NotNull Block block, @NotNull BlockStateCheck check) implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, BlockStateRequirement> CONVERTER = new LootConverter<>("minecraft:block_state_property", BlockStateRequirement.class) {
        @Override
        public @NotNull BlockStateRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            String rawBlock = JsonUtils.assureString(json.get("block"), "block");
            Block actualBlock = Block.fromNamespaceId(rawBlock);
            if (actualBlock == null) {
                throw new LootConversionException(JsonUtils.createExpectedValueMessage("a valid Block", "block", null));
            }
            BlockStateCheck check = BlockStateCheck.deserialize(json.get("properties"), context);
            return new BlockStateRequirement(actualBlock, check);
        }

        @Override
        public void serialize(@NotNull BlockStateRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            result.addProperty("block", input.block().name());
            if (!input.check().checks().isEmpty()) {
                result.add("properties", BlockStateCheck.serialize(input.check(), context));
            }
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
