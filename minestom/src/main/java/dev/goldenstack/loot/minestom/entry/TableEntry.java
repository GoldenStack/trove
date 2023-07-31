package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

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
                        @NotNull List<LootCondition> conditions) implements StandardSingleChoice {

    public static final @NotNull String KEY = "minecraft:loot_table";

    /**
     * A standard map-based converter for table entries.
     */
    public static final @NotNull TypedLootConverter<TableEntry> CONVERTER =
            converter(TableEntry.class,
                    type(NamespaceID.class).name("tableIdentifier").nodePath("name"),
                    type(long.class).name("weight").withDefault(1L),
                    type(long.class).name("quality").withDefault(0L),
                    typeList(LootModifier.class).name("modifiers").nodePath("functions").withDefault(List::of),
                    typeList(LootCondition.class).name("conditions").withDefault(List::of)
            );

    public TableEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean shouldGenerate(@NotNull LootContext context) {
        return LootCondition.all(conditions(), context);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootContext context) {
        if (!context.has(LootContextKeys.REGISTERED_TABLES)) {
            return LootBatch.of();
        }
        LootGenerator table = context.assure(LootContextKeys.REGISTERED_TABLES).get(tableIdentifier);
        return table == null ? LootBatch.of() : LootModifier.applyAll(modifiers(), table.generate(context), context);
    }
}