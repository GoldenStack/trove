package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

/**
 * An entry that always returns an empty list of items.
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record EmptyEntry(long weight, long quality,
                         @NotNull List<LootModifier> modifiers,
                         @NotNull List<LootCondition> conditions) implements StandardSingleChoice {

    public static final @NotNull String KEY = "minecraft:empty";

    /**
     * A standard map-based converter for empty entries.
     */
    public static final @NotNull TypedLootConverter<EmptyEntry> CONVERTER =
            converter(EmptyEntry.class,
                    type(long.class).name("weight").withDefault(1L),
                    type(long.class).name("quality").withDefault(0L),
                    typeList(LootModifier.class).name("modifiers").nodePath("functions").withDefault(List::of),
                    typeList(LootCondition.class).name("conditions").withDefault(List::of)
            );

    public EmptyEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean shouldGenerate(@NotNull LootContext context) {
        return LootCondition.all(conditions(), context);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootContext context) {
        return LootBatch.EMPTY;
    }
}
