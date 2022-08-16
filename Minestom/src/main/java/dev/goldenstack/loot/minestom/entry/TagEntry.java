package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.spongepowered.configurate.ConfigurateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adds items from the tag ({@link #tag()}. Invalid identifiers will be ignored.
 * @param tag the tag to get item IDs from.
 * @param expand true if each item in the tag should be its own option, and false if they should all be in the same
 *               option.
 * @param weight the base weight of this entry - see {@link StandardWeightedOption#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedOption#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record TagEntry(@NotNull Tag tag, boolean expand,
                       long weight, long quality,
                       @NotNull List<LootModifier<ItemStack>> modifiers,
                       @NotNull List<LootCondition<ItemStack>> conditions) implements SingleOptionEntry<ItemStack>, StandardWeightedOption<ItemStack> {

    /**
     * A standard map-based converter for tag entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, TagEntry> CONVERTER = Utils.createKeyedConverter("minecraft:tag", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("name").set(input.tag().getName().asString());
                result.node("expand").set(input.expand);
                result.node("weight").set(input.weight);
                result.node("quality").set(input.quality);
                result.node("functions").set(Utils.serializeList(input.modifiers(), context.loader().lootModifierManager()::serialize, context));
                result.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
            }, (input, context) -> {
                var nameNode = input.node("name");
                String name = Utils.require(nameNode, String.class);
                Tag tag = MinecraftServer.getTagManager().getTag(Tag.BasicType.ITEMS, name);
                if (tag == null) {
                    throw new ConfigurateException(nameNode, "Expected the provided node to have a valid item tag, but found '" + name + "' instead.");
                }
                return new TagEntry(
                        tag,
                        input.node("expand").getBoolean(),
                        input.node("weight").getLong(1),
                        input.node("quality").getLong(0),
                        Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context),
                        Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context)
                );
            });

    public TagEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Option<ItemStack>> requestOptions(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        if (expand) {
            List<Option<ItemStack>> options = new ArrayList<>();
            for (ItemStack tagItem : generate(context)) {
                options.add(new Option<>() {
                    @Override
                    public @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootGenerationContext context) {
                        return TagEntry.this.getWeight(context);
                    }

                    @Override
                    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
                        return List.of(tagItem);
                    }
                });
            }
            return options;
        } else {
            return List.of(this);
        }
    }

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        return LootModifier.applyAll(modifiers(),
                tag.getValues().stream().map(Material::fromNamespaceId).filter(Objects::nonNull).map(ItemStack::of).toList(),
                context
        );
    }
}
