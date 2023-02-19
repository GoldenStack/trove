package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * An entry that always returns an empty list of items.
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record EmptyEntry(long weight, long quality,
                         @NotNull List<LootModifier> modifiers,
                         @NotNull List<LootCondition> conditions) implements SingleChoiceEntry, StandardWeightedChoice {

    /**
     * A standard map-based converter for empty entries.
     */
    public static final @NotNull KeyedLootConverter<EmptyEntry> CONVERTER =
            converter(EmptyEntry.class,
                    implicit(long.class).name("weight").withDefault(1L),
                    implicit(long.class).name("quality").withDefault(0L),
                    modifier().list().name("modifiers").nodeName("functions").withDefault(ArrayList::new),
                    condition().list().name("conditions").withDefault(ArrayList::new)
            ).keyed("minecraft:empty");

    public EmptyEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootGenerationContext context) {
        return LootBatch.EMPTY;
    }
}
