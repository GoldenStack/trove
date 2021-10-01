package dev.goldenstack.loot.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents something that can return true or false based on the LootContext that is provided
 */
public interface LootCondition extends LootSerializer<LootCondition>, Predicate<LootContext> {

    /**
     * Returns true or false based on the LootContext.<br>
     * The only reason this is being overridden is so that people have to use {@link NotNull @NotNull}
     */
    @Override
    boolean test(@NotNull LootContext context);

    /**
     * Returns true if at least one of the conditions is true. If there are no conditions, it returns false.
     * @param context The context to use for verification
     * @param conditions The conditions
     * @return True if at least one condition is true
     */
    static boolean or(@NotNull LootContext context, @NotNull List<LootCondition> conditions){
        if (conditions.size() == 0){
            return false;
        }
        if (conditions.size() == 1){
            return conditions.get(0).test(context);
        }
        for (LootCondition condition : conditions){
            if (condition.test(context)){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if all the conditions are true. If there are no conditions, it returns true.
     * @param context The context to use for verification
     * @param conditions The conditions
     * @return True if all conditions are true
     */
    static boolean and(@NotNull LootContext context, @NotNull List<LootCondition> conditions){
        if (conditions.size() == 0){
            return true;
        }
        if (conditions.size() == 1){
            return conditions.get(0).test(context);
        }
        for (LootCondition condition : conditions){
            if (!condition.test(context)){
                return false;
            }
        }
        return true;
    }
}