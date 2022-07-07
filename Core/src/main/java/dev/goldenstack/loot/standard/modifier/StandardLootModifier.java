package dev.goldenstack.loot.standard.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootRequirement;
import dev.goldenstack.loot.util.LootRequirementHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Standard class for loot modifiers that is intended to make it easier to implement some basic features
 * @param <L> the loot item
 */
public abstract class StandardLootModifier<L> implements LootModifier<L>, LootRequirementHolder<L> {

    private final @NotNull List<LootRequirement<L>> requirements;

    /**
     * @param requirements see {@link #requirements()}
     */
    public StandardLootModifier(@NotNull List<LootRequirement<L>> requirements) {
        this.requirements = List.copyOf(requirements);
    }

    /**
     * @return the list of the requirements that are used to determine if any provided loot items should be modified
     */
    @Override
    public final @NotNull List<LootRequirement<L>> requirements() {
        return requirements;
    }

    /**
     * @return {@link #rawModify(Object, LootContext)} if {@link #passes(LootContext)} is true, otherwise an empty list
     */
    @Override
    public final @NotNull L modify(@NotNull L lootItem, @NotNull LootContext context) {
        return passes(context) ? rawModify(lootItem, context) : lootItem;
    }

    /**
     * The basic modification to objects that are provided.
     * @param lootItem the item to potentially modify
     * @param context the context to use for modification, if required
     * @return the modified form of the item
     */
    protected abstract @NotNull L rawModify(@NotNull L lootItem, @NotNull LootContext context);
}
