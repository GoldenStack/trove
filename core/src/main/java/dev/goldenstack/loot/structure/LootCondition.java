package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Verifies a loot context, potentially determining if something else should be done.
 */
public interface LootCondition {

    /**
     * Attempts to validate the provided loot context, returning the result.
     * @param context the context object, to use if required
     * @return true if the provided loot context is valid according to this condition
     */
    boolean verify(@NotNull LootGenerationContext context);

    /**
     * Checks to see if at least one condition in the provided collection verifies the context. After one condition
     * verifies it, the others are skipped (as the result is already known), so do not rely on any specific number of
     * these conditions being called.<br>
     * If the provided list is empty, the result is always false.
     * @param conditions the collection of conditions to check
     * @param context the context, to feed to the conditions when they are being tested
     * @return true if at least one condition in the provided collection verifies the provided context
     */
    static boolean or(@NotNull Collection<LootCondition> conditions, @NotNull LootGenerationContext context) {
        if (conditions.isEmpty()) {
            return false;
        }
        for (var condition : conditions) {
            if (condition.verify(context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if every condition in the provided collection verifies the context. After one condition doesn't
     * verify it, the others are skipped (as the result is already known), so do not rely on any specific number of
     * these conditions being called.<br>
     * If the provided list is empty, the result is always true.
     * @param conditions the collection of conditions to check
     * @param context the context, to feed to the conditions when they are being tested
     * @return true if every condition in the provided collection verifies the provided context
     */
    static boolean all(@NotNull Collection<LootCondition> conditions, @NotNull LootGenerationContext context) {
        if (conditions.isEmpty()) {
            return true;
        }
        for (var condition : conditions) {
            if (!condition.verify(context)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if at least {@code required} conditions in the provided collection return true. At any point, if at
     * least the required number of conditions have returned true, or it is impossible for there to be enough conditions
     * that return true to have at least the required number, false is returned.<br>
     * Do not rely on any specific number of these conditions being called.
     * @param conditions the collection of conditions to check
     * @param required the minimum number of conditions in the provided collection that must verify the context
     * @param context the context, to feed to the conditions when they are being tested
     * @return true if at least {@code required} conditions in the provided collection verify the context
     */
    static boolean some(@NotNull Collection<LootCondition> conditions, int required, @NotNull LootGenerationContext context) {
        if (required <= 0) {
            return true;
        } else if (required > conditions.size()) {
            return false;
        } else if (required == 1) {
            return or(conditions, context);
        } else if (required == conditions.size()) {
            return all(conditions, context);
        }

        int passed = 0;
        int possiblePassed = conditions.size();
        for (var condition : conditions) {
            if (condition.verify(context)) {
                passed++;
            }
            possiblePassed--;

            if (passed + possiblePassed < required) {
                return false;
            } else if (passed >= required) {
                return true;
            }
        }
        // Theoretically, this line cannot be reached unless something goes very wrong.
        return true;
    }

}
