package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextCriterion;
import dev.goldenstack.loot.json.LootAware;
import dev.goldenstack.loot.util.LootModifierHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot table stores a list of loot pools, loot modifiers, and required criterion.
 * @param criterion stores the required loot context keys
 * @param pools the pools that will be asked for loot
 * @param modifiers modifiers to apply to each and every loot item that is generated from the pools
 * @param <L> the loot item
 */
public record LootTable<L>(@NotNull LootContextCriterion criterion,
                           @NotNull List<LootPool<L>> pools,
                           @NotNull List<LootModifier<L>> modifiers) implements LootAware<L>, LootModifierHolder<L> {

    public LootTable {
        pools = List.copyOf(pools);
        modifiers = List.copyOf(modifiers);
    }

    /**
     * Generates a list of loot items. This is pretty simple: it just gets the loot from each pool and then applies
     * every modifier to it.<br>
     * Note: The returned list may or may not be immutable.<br>
     * @param context the context to use for generation
     * @return every loot item that was generated
     */
    public @NotNull List<L> generate(@NotNull LootContext context) throws IllegalArgumentException {
        if (!criterion.fulfills(context)) {
            throw new IllegalArgumentException("Invalid loot context: all keys in this table's criterion must be held by the provided loot context");
        }

        // No loot can be generated
        if (this.pools.isEmpty()) {
            return List.of();
        }

        List<L> items = new ArrayList<>();
        for (LootPool<L> pool : pools) {
            for (L lootItem : pool.generate(context)) {
                items.add(modify(lootItem, context));
            }
        }
        return items;
    }
}
