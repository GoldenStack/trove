package dev.goldenstack.loot.util;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.conversion.LootAware;
import dev.goldenstack.loot.structure.LootRequirement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Indicates that instances of a class can have a list of requirements.
 * @param <L> the loot item
 */
public interface LootRequirementHolder<L> extends LootAware<L> {

    /**
     * @return the list of requirements held by this object
     */
    @NotNull List<LootRequirement<L>> requirements();

    /**
     * @param context the context to check the conditions with
     * @return true if all of the requirements pass, otherwise false
     */
    default boolean passes(@NotNull LootContext context) {
        return LootRequirement.all(context, requirements());
    }
}
