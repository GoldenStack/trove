package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A function that allows loot to pass through it, potentially making modifications.
 * @param <L> the loot item type
 */
public interface LootModifier<L> {

    /**
     * Performs any mutations on the provided object and returns the result.
     * @param input the input item to this modifier. When providing this parameter, be aware of the fact that the
     *              parameter itself could potentially be modified.
     * @param context the context object, to use if required
     * @return the modified form of the input
     */
    @NotNull L modify(@NotNull L input, @NotNull LootGenerationContext context);

    /**
     * Applies all of the provided modifiers to the provided input. The order in which they are applied is equal to the
     * order provided by the iterator of the provided collection of modifiers.
     * @param modifiers the modifiers to apply
     * @param input the initial input to pass through the modifiers
     * @param context the context object, to use if required
     * @return the item with all modifiers applied
     * @param <L> the loot item type
     */
    static <L> @NotNull L applyAll(@NotNull Collection<LootModifier<L>> modifiers, @NotNull L input, @NotNull LootGenerationContext context) {
        if (modifiers.isEmpty()) {
            return input;
        }
        for (var modifier : modifiers) {
            input = modifier.modify(input, context);
        }
        return input;
    }

    /**
     * Applies all of the provided modifiers to each of the provided input items. The order in which they are applied to
     * each item is equal to the order provided by the iterator of the provided collection of modifiers, and the order
     * in which each individual item is modified should not matter.
     * @param modifiers the modifiers to apply
     * @param input the input items to apply each modifier to
     * @param context the context object, to use if required
     * @return a list containing the fully modified versions of all of the provided items
     * @param <L> the loot item type
     */
    static <L> @NotNull List<L> applyAll(@NotNull Collection<LootModifier<L>> modifiers, @NotNull Collection<L> input, @NotNull LootGenerationContext context) {
        if (input.isEmpty()) {
            return List.of();
        } if (modifiers.isEmpty()) {
            return List.copyOf(input);
        }
        List<L> generated = new ArrayList<>(input.size());
        for (var item : input) {
            for (var modifier : modifiers) {
                item = modifier.modify(item, context);
            }
            generated.add(item);
        }
        return generated;
    }

}
