package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * Adds items from the tag ({@link #itemTag()}. Invalid identifiers will be ignored.
 * @param itemTag the tag to get item IDs from.
 * @param expand true if each item in the tag should be its own choice, and false if they should all be in the same
 *               choice.
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record TagEntry(@NotNull Tag itemTag, boolean expand,
                       long weight, long quality,
                       @NotNull List<LootModifier> modifiers,
                       @NotNull List<LootCondition> conditions) implements StandardSingleChoice {

    /**
     * A standard map-based converter for tag entries.
     */
    public static final @NotNull KeyedLootConverter<TagEntry> CONVERTER =
            converter(TagEntry.class,
                    tag(Tag.BasicType.ITEMS).name("itemTag").nodePath("name"),
                    implicit(boolean.class).name("expand"),
                    implicit(long.class).name("weight").withDefault(1L),
                    implicit(long.class).name("quality").withDefault(0L),
                    modifier().list().name("modifiers").nodePath("functions").withDefault(List::of),
                    condition().list().name("conditions").withDefault(List::of)
            ).keyed("minecraft:tag");

    public TagEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        if (expand) {
            List<Choice> choices = new ArrayList<>();
            for (Object tagItem : generate(context).items()) {
                choices.add(new Choice() {
                    @Override
                    public @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context) {
                        return TagEntry.this.getWeight(context);
                    }

                    @Override
                    public @NotNull LootBatch generate(@NotNull LootContext context) {
                        return LootBatch.of(tagItem);
                    }
                });
            }
            return choices;
        } else {
            return List.of(this);
        }
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootContext context) {
        return LootModifier.applyAll(modifiers(),
                LootBatch.of(itemTag.getValues().stream().map(Material::fromNamespaceId).filter(Objects::nonNull).map(ItemStack::of).toList()),
                context
        );
    }
}
