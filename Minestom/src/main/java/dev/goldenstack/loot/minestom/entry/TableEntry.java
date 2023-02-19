package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * An entry that is dynamically linked to a loot table using {@link LootContextKeys#REGISTERED_TABLES} and {@link #tableIdentifier()}.
 * @param tableIdentifier the identifier to look for tables with
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record TableEntry(@NotNull NamespaceID tableIdentifier,
                        long weight, long quality,
                        @NotNull List<LootModifier> modifiers,
                        @NotNull List<LootCondition> conditions) implements SingleChoiceEntry, StandardWeightedChoice {

    /**
     * A standard map-based converter for table entries.
     */
    public static final @NotNull KeyedLootConverter<TableEntry> CONVERTER =
            converter(TableEntry.class,
                    namespaceId().name("tableIdentifier").nodeName("name"),
                    implicit(long.class).name("weight").withDefault(1L),
                    implicit(long.class).name("quality").withDefault(0L),
                    modifier().list().name("modifiers").nodeName("functions").withDefault(ArrayList::new),
                    condition().list().name("conditions").withDefault(ArrayList::new)
            ).keyed("minecraft:loot_table");

    public TableEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context) || !context.has(LootContextKeys.REGISTERED_TABLES)) {
            return LootBatch.of();
        }
        LootGenerator table = context.assure(LootContextKeys.REGISTERED_TABLES).get(tableIdentifier);
        return table == null ? LootBatch.of() : LootModifier.applyAll(modifiers(), table.generate(context), context);
    }
}