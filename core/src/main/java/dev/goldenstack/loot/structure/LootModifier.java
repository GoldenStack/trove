package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootContext;
import io.leangen.geantyref.GenericTypeReflector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
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
    @NotNull Object modify(@NotNull Object input, @NotNull LootContext context);

    /**
     * A function that only modifies a specific type, calling the individual modifier
     * {@link #modifyTyped(Object, LootContext)} for each valid instance in each provided batch.
     * @param <T> the type to filter
     */
    interface Filtered<T> extends LootModifier {

        /**
         * Returns {@link #modifyTyped(Object, LootContext)} if the input that is a subtype of {@link #filteredType()}.
         */
        @SuppressWarnings("unchecked")
        @Override
        default @NotNull Object modify(@NotNull Object input, @NotNull LootContext context) {
            // Casting should be safe as we verified the type
            return GenericTypeReflector.isSuperType(filteredType(), input.getClass()) ?
                    modifyTyped((T) input, context) : input;
        }

        /**
         * Filters an instance of {@link #filteredType()}.
         * @param input the input object to filter
         * @param context the context object, to use if required
         * @return the modified object (of any type), or null if the input object shouldn't be added to the filtered
         *         batch for some reason
         */
        @NotNull Object modifyTyped(@NotNull T input, @NotNull LootContext context);

        /**
         * Returns the type that this filtered modifier will modify. This will be used with
         * {@link GenericTypeReflector#isSuperType(Type, Type)} without catching class cast exceptions, so unexpected
         * behaviour may occur if this doesn't match {@link T}!
         * @return the filtered type
         */
        @NotNull Type filteredType();

    }

    /**
     * Applies each modifier to the given item.
     * @param modifiers the modifiers to apply
     * @param object the object to modify
     * @param context the context to use
     * @return the modified object
     */
    static @NotNull Object apply(@NotNull Collection<LootModifier> modifiers, @NotNull Object object, @NotNull LootContext context) {
        for (var modifier : modifiers) {
            object = modifier.modify(object, context);
        }
        return object;
    }

}
