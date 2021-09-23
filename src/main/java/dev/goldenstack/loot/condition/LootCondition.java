package dev.goldenstack.loot.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents something that can return true or false based on the LootContext that is provided
 */
public interface LootCondition extends LootSerializer<LootCondition>, Predicate<LootContext> {

    // This is here just to make people use @NotNull

    /**
     * Returns true or false based on the LootContext
     */
    @Override
    boolean test(@NotNull LootContext context);
}