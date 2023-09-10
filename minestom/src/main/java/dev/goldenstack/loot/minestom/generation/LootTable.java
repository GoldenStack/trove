package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.generation.LootProcessor;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

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

    public static final @NotNull TypeSerializer<LootTable> SERIALIZER =
            serializer(LootTable.class,
                    field(LootContextKeyGroup.class).name("contextKeyGroup").nodePath("type"),
                    field(LootPool.class).name("pools").as(list()).fallback(List::of),
                    field(LootModifier.class).name("modifiers").nodePath("functions").as(list()).fallback(List::of)
            );

    public LootTable {
        pools = List.copyOf(pools);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public void accept(@NotNull LootContext context, @NotNull LootProcessor processor) {
        // Make sure that this table's required keys are in the given context
        contextKeyGroup.assureVerified(context);

        for (var pool : pools) {
            pool.accept(context, (c, object) -> processor.accept(c, LootModifier.apply(modifiers(), object, c)));
        }
    }

}
