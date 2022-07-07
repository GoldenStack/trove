package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.conversion.LootAware;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Checks the provided loot context, assuring that it's valid
 * @param <L> the loot item
 */
public interface LootRequirement<L> extends LootAware<L> {

    /**
     * Checks the provided loot context, returning true or false based on it
     * @param context the loot context that will be verified
     * @return whether or not the provided context passes this requirement
     */
    boolean check(@NotNull LootContext context);

    /**
     * Note: This method may end up calling any number of the requirements from the provided list. Do not rely on
     * whether or not none, all, or any number of the specific requirements in this list are checked.
     * @param context the context to feed into the requirements
     * @param requirements the requirements that will be tested
     * @return true if at least one of the provided requirements passed, and false if none did
     * @param <L> the loot item
     */
    static <L> boolean or(@NotNull LootContext context, @NotNull List<LootRequirement<L>> requirements) {
        if (requirements.isEmpty()) {
            return false;
        }
        for (LootRequirement<L> requirement : requirements) {
            if (requirement.check(context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Note: This method may end up calling any number of the requirements from the provided list. Do not rely on
     * whether or not none, all, or any number of the specific requirements in this list are checked.
     * @param context the context to feed into the requirements
     * @param requirements the requirements that will be tested
     * @return true if all of the provided requirements passed, and false if at least one didn't
     * @param <L> the loot item
     */
    static <L> boolean all(@NotNull LootContext context, @NotNull List<LootRequirement<L>> requirements) {
        if (requirements.isEmpty()) {
            return true;
        }
        for (LootRequirement<L> requirement : requirements) {
            if (!requirement.check(context)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Note: This method may end up calling any number of the requirements from the provided list. Do not rely on none,
     * all, or any number of the specific requirements in this list are checked.
     * @param context the context to feed into the requirements
     * @param requirements the requirements that will be tested
     * @param required the required number of requirements to pass
     * @return true if at least {@code required} requirements passed, and false if not
     * @param <L> the loot item
     */
    static <L> boolean some(@NotNull LootContext context, @NotNull List<LootRequirement<L>> requirements, int required) {
        if (required <= 0) {
            return true;
        } else if (required > requirements.size()) {
            return false;
        } else if (required == 1) {
            return or(context, requirements);
        } else if (required == requirements.size()) {
            return all(context, requirements);
        }

        int passed = 0;
        for (int i = 0; i < requirements.size(); i++) {
            // Check the current requirement first because of the previous checks done
            if (requirements.get(i).check(context)) {
                passed++;
            }
            // Determine if it is possible to pass all of the requirements at this point
            int possiblePassed = requirements.size() - (i + 1);
            if (passed + possiblePassed < required) {
                return false;
            } else if (passed >= required) {
                return true;
            }
        }
        return true;
    }

}