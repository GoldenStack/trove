package dev.goldenstack.loot.structure;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.generation.LootBatch;
import io.leangen.geantyref.GenericTypeReflector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * A function that only modifies a specific type, calling the individual modifier
     * {@link #modify(Object, LootGenerationContext)} for each valid instance in each provided batch.
     * @param <T> the type to filter
     */
    interface Filtered<T> extends LootModifier {

        /**
         * Calls {@link #modify(Object, LootGenerationContext)} for each input that is a subtype of
         * {@link #filteredType()}.
         */
        @SuppressWarnings("unchecked")
        @Override
        default @NotNull LootBatch modify(@NotNull LootBatch input, @NotNull LootGenerationContext context) {
            List<Object> filtered = new ArrayList<>();
            for (var item : input.items()) {
                if (GenericTypeReflector.isSuperType(filteredType(), item.getClass())) {
                    var modified = modify((T) item, context);
                    if (modified != null) {
                        filtered.add(item);
                    }
                } else {
                    filtered.add(item);
                }
            }

            return LootBatch.of(filtered);
        }

        /**
         * Filters an instance of {@link #filteredType()}.
         * @param input the input object to filter
         * @param context the context object, to use if required
         * @return the modified object (of any type), or null if the input object shouldn't be added to the filtered
         *         batch for some reason
         */
        @Nullable Object modify(@NotNull T input, @NotNull LootGenerationContext context);

        /**
         * Returns the type that this filtered modifier will modify. This will be used with
         * {@link GenericTypeReflector#isSuperType(Type, Type)} without catching class cast exceptions, so unexpected
         * behaviour may occur if this doesn't match {@link T}!
         * @return the filtered type
         */
        @NotNull Type filteredType();

    }

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
