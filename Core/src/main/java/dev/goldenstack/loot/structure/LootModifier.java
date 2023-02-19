package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.generation.LootBatch;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A function that allows loot to pass through it, potentially making modifications.
 */
public interface LootModifier {

    /**
     * Performs any mutations on the provided object and returns the result.
     * @param input the input item to this modifier. When providing this parameter, be aware of the fact that the
     *              parameter itself could potentially be modified.
     * @param context the context object, to use if required
     * @return the modified form of the input
     */
    @NotNull LootBatch modify(@NotNull LootBatch input, @NotNull LootGenerationContext context);

    /**
     * Applies all of the provided modifiers to the provided input. The order in which they are applied is equal to the
     * order provided by the iterator of the provided collection of modifiers.
     * @param modifiers the modifiers to apply
     * @param input the initial input to pass through the modifiers
     * @param context the context object, to use if required
     * @return the item with all modifiers applied
     */
    static @NotNull LootBatch applyAll(@NotNull Collection<LootModifier> modifiers, @NotNull LootBatch input, @NotNull LootGenerationContext context) {
        if (modifiers.isEmpty()) {
            return input;
        }
        for (var modifier : modifiers) {
            input = modifier.modify(input, context);
        }
        return input;
    }

}
