package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

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
     * A standard map-based serializer for table entries.
     */
    public static final @NotNull TypeSerializer<TableEntry> SERIALIZER =
            serializer(TableEntry.class,
                    field(NamespaceID.class).name("tableIdentifier").nodePath("name"),
                    field(long.class).name("weight").fallback(1L),
                    field(long.class).name("quality").fallback(0L),
                    field(LootModifier.class).name("modifiers").nodePath("functions").as(list()).fallback(List::of),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
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