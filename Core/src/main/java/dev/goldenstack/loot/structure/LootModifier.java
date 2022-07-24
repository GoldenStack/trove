package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;

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

}
