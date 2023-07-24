package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.FieldTypes.condition;
import static dev.goldenstack.loot.converter.generator.FieldTypes.implicit;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.block;

/**
 * Copies the properties in {@link #copiedProperties()} onto the provided item if the context's block state key is equal
 * to {@link #blockType()}.
 * @param conditions the conditions required for use
 * @param blockType the block type that the context's {@link LootContextKeys#BLOCK_STATE} must be equal to
 * @param copiedProperties the list of properties to copy
 */
public record CopyStateModifier(@NotNull List<LootCondition> conditions, @NotNull Block blockType,
                                @NotNull List<String> copiedProperties) implements ItemStackModifier {

    /**
     * A standard map-based converter for copy state modifiers.
     */
    public static final @NotNull KeyedLootConverter<CopyStateModifier> CONVERTER =
            converter(CopyStateModifier.class,
                    condition().list().name("conditions").withDefault(List::of),
                    block().name("blockType").nodePath("block"),
                    implicit(String.class).list().name("copiedProperties").nodePath("properties")
            ).keyed("minecraft:copy_state");

    @SuppressWarnings("UnstableApiUsage")
    private static final @NotNull Tag<NBTCompound> BLOCK_STATE_TAG = Tag.Structure("BlockStateTag", TagSerializer.COMPOUND).defaultValue(new NBTCompound());

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context) || !context.has(LootContextKeys.BLOCK_STATE)) {
            return input;
        }

        var blockProperties = context.assure(LootContextKeys.BLOCK_STATE).properties();
        var blockStateTag = input.getTag(BLOCK_STATE_TAG).toMutableCompound();

        for (var property : this.copiedProperties) {
            if (blockProperties.containsKey(property)) {
                blockStateTag.setString(property, blockProperties.get(property));
            }
        }

        return input.withTag(BLOCK_STATE_TAG, blockStateTag.toCompound());
    }
}
