package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;


/**
 * A standard loot table implementation for Minestom.
 * @param contextKeyGroup the context key group that all provided context objects must pass
 * @param pools the pools that will generate loot
 * @param modifiers the modifiers that are applied to each piece of loot
 */
public record LootTable(@NotNull LootContextKeyGroup contextKeyGroup,
                        @NotNull List<LootPool> pools,
                        @NotNull List<LootModifier> modifiers) implements LootGenerator {

    /**
     * A completely empty loot table that never returns any loot.
     */
    public static final @NotNull LootTable EMPTY = new LootTable(LootContextKeyGroup.EMPTY, List.of(), List.of());

    public static final @NotNull TypedLootConverter<LootTable> CONVERTER =
            converter(LootTable.class,
                    type(LootContextKeyGroup.class).name("contextKeyGroup").nodePath("type"),
                    typeList(LootPool.class).name("pools").withDefault(List::of),
                    typeList(LootModifier.class).name("modifiers").nodePath("functions").withDefault(List::of)
            );

    public LootTable {
        pools = List.copyOf(pools);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootContext context) {
        // Make sure that this table's required keys are in the given context
        contextKeyGroup.assureVerified(context);

        if (pools.isEmpty()) {
            return LootBatch.of();
        }

        List<Object> items = new ArrayList<>();
        for (var pool : pools) {
            items.addAll(LootModifier.applyAll(modifiers(), pool.generate(context), context).items());
        }

        return LootBatch.of(items);
    }

}
