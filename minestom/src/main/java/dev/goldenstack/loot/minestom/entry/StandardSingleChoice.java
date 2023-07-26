package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A standard single choice entry that only returns itself under certain conditions.
 */
public interface StandardSingleChoice extends LootEntry, LootEntry.Choice, StandardWeightedChoice {

    /**
     * Determines whether or not this should return any choices.
     * @param context the context to use
     * @return true if this choice should generate a choice
     */
    default boolean shouldGenerate(@NotNull LootContext context) {
        return true;
    }

    /**
     * Requests choices, returning none if {@link #shouldGenerate(LootContext)} is not true;
     * {@inheritDoc}
     */
    @Override
    default @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
        return shouldGenerate(context) ? List.of(this) : List.of();
    }


}
