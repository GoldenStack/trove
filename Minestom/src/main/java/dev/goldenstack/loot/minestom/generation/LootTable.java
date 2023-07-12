package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;


/**
 * A standard loot table implementation for Minestom.
 * @param contextKeyGroup the context key group that all provided context objects must pass
 * @param pools the pools that will generate loot
 * @param modifiers the modifiers that are applied to each piece of loot
 */
public record LootTable(@NotNull LootContextKeyGroup contextKeyGroup,
                        @NotNull List<LootPool> pools,
                        @NotNull List<LootModifier> modifiers) implements LootGenerator {

    public static final @NotNull AdditiveConverter<LootTable> CONVERTER =
            converter(LootTable.class,
                    keyGroup().name("contextKeyGroup").nodePath("type"),
                    pool().list().name("pools").withDefault(List::of),
                    modifier().list().name("modifiers").nodePath("functions").withDefault(List::of)
            ).additive();

    public LootTable {
        pools = List.copyOf(pools);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootGenerationContext context) {
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

    /**
     * Creates a new builder for this class, with no pools and modifiers and a null context key group.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new loot table builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LootContextKeyGroup contextKeyGroup;
        private final @NotNull List<LootPool> pools = new ArrayList<>();
        private final @NotNull List<LootModifier> modifiers = new ArrayList<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder contextKeyGroup(@NotNull LootContextKeyGroup contextKeyGroup) {
            this.contextKeyGroup = contextKeyGroup;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addPool(@NotNull LootPool pool) {
            this.pools.add(pool);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addModifier(@NotNull LootModifier modifier) {
            this.modifiers.add(modifier);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootTable build() {
            return new LootTable(
                    Objects.requireNonNull(contextKeyGroup, "Standard loot tables must have a context key group!"),
                    pools,
                    modifiers
            );
        }

    }
}
