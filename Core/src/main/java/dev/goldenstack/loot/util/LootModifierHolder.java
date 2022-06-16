package dev.goldenstack.loot.util;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootAware;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Indicates that instances of a class can have a list of modifiers.
 * @param <L> the loot item
 */
public interface LootModifierHolder<L> extends LootAware<L> {

    /**
     * @return the list of modifiers held by this object
     */
    @NotNull List<LootModifier<L>> modifiers();

    /**
     * @return the combined result of all {@link #modifiers()} applied sequentially (i.e., in order of
     * {@code modifiers().iterator()}) to the provided loot item
     */
    default @NotNull L modify(@NotNull L lootItem, @NotNull LootContext context) {
        if (modifiers().isEmpty()) {
            return lootItem;
        }
        for (LootModifier<L> modifier : modifiers()) {
            lootItem = modifier.modify(lootItem, context);
        }
        return lootItem;
    }
}
