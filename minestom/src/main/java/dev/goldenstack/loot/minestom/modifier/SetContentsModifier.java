package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.generation.LootProcessor;
import dev.goldenstack.loot.minestom.util.nbt.NBTUtils;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Sets the contents of each provided item (stored in NBT as BlockEntityTag.Items) based on the {@link #entries()}.
 * @param conditions the conditions required for items to be added
 * @param entries the loot entries that generate the items to add
 * @param blockEntityKey the ID of the block entity to use
 */
public record SetContentsModifier(@NotNull List<LootCondition> conditions,
                                  @NotNull List<LootEntry> entries,
                                  @NotNull NamespaceID blockEntityKey) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:set_contents";

    /**
     * A standard map-based serializer for set contents modifiers.
     */
    public static final @NotNull TypeSerializer<SetContentsModifier> SERIALIZER =
            serializer(SetContentsModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(LootEntry.class).name("entries").as(list()),
                    field(NamespaceID.class).name("blockEntityKey").nodePath("type")
            );

    @SuppressWarnings("UnstableApiUsage")
    private static final @NotNull Tag<NBTCompound> BLOCK_ENTITY_TAG = Tag.Structure("BlockEntityTag", TagSerializer.COMPOUND);

    @Override
    public @NotNull Object modifyTyped(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context) || input.isAir()) {
            return input;
        }

        List<ItemStack> items = new ArrayList<>();

        // Create loot processor
        var processor = LootProcessor.processClass(ItemStack.class, item -> {
            var rule = StackingRule.get();

            var maxSize = rule.getMaxSize(item);
            var count = rule.getAmount(item);

            while (count > maxSize) {
                if (rule.canApply(item, maxSize)) {
                    items.add(rule.apply(item, maxSize));
                }
                count -= maxSize;
            }

            if (count > 0) {
                if (rule.canApply(item, count)) {
                    items.add(rule.apply(item, count));
                }
            }
        });

        // Actually process everything
        for (var entry : this.entries) {
            for (var choice : entry.requestChoices(context)) {
                choice.accept(context, processor);
            }
        }

        // All we want to override are the items and the new ID.
        NBTCompound newTag = NBT.Compound(Map.of(
                "Items", NBTUtils.itemsToList(items),
                "id", NBT.String(blockEntityKey.asString())
        ));

        NBTCompound oldTag = input.hasTag(BLOCK_ENTITY_TAG) ?
                NBTUtils.merge(input.getTag(BLOCK_ENTITY_TAG), newTag) : newTag;

        return input.withMeta(builder -> builder.setTag(BLOCK_ENTITY_TAG, oldTag));
    }

}
