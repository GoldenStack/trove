package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootAware;
import org.jetbrains.annotations.NotNull;

/**
 * Allows a loot item to pass through a modifier that may potentially make changes to it.
 * @param <L> the loot item
 */
public interface LootModifier<L> extends LootAware<L> {

    /**
     * Applies this loot modifier to the provided loot item, returning the result.
     * @param lootItem the loot item that is being passed through this modifier
     * @param context the context, to allow the modifier to have more, well, context
     * @return the potentially modified item
     */
    @NotNull L modify(@NotNull L lootItem, @NotNull LootContext context);
}
