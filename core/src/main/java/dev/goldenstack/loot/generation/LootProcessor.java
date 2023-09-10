package dev.goldenstack.loot.generation;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * Represents some black box that can process loot.
 */
public interface LootProcessor extends BiConsumer<@NotNull LootContext, @NotNull Object> {

    /**
     * Processes the given object.
     * @param context the context
     * @param object the object that needs to be processed
     */
    @Override
    void accept(@NotNull LootContext context, @NotNull Object object);

    /**
     * A loot processor that can indicate when it can process something.
     */
    interface Filtered extends LootProcessor {

        /**
         * Returns whether or not this processor can process the given object.
         * @param context the context
         * @param object the object that needs to be processed
         * @return true if this processor can process the provided object, false if not
         */
        boolean canProcess(@NotNull LootContext context, @NotNull Object object);

    }

    /**
     * Joins the given filtered processors into a single one.
     * @param children the filtered processors to join
     * @return the created processor
     */
    static @NotNull Filtered join(@NotNull List<Filtered> children) {
        return new FilteredImpl((c, o) -> children.stream().anyMatch(a -> a.canProcess(c, o)), (c, o) -> {
            for (var child : children) {
                if (child.canProcess(c, o)) {
                    child.accept(c, o);
                    return;
                }
            }

            throw new IllegalArgumentException("Cannot process object '" + o + "'");
        });
    }

    /**
     * Creates a filtered processor with the given predicate and consumer.
     * @param predicate the predicate to use
     * @param consumer the processor consumer
     * @return the created processor
     */
    static @NotNull Filtered filtered(@NotNull BiPredicate<@NotNull LootContext, @NotNull Object> predicate,
                                      @NotNull BiConsumer<@NotNull LootContext, @NotNull Object> consumer) {
        return new FilteredImpl(predicate, consumer);
    }

    /**
     * Creates a filtered processor that processes exclusively the provided class.
     * @param type the class to convert
     * @param consumer the processor consumer
     * @return the created processor
     * @param <T> the converted type
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull Filtered typed(@NotNull Class<T> type, @NotNull BiConsumer<@NotNull LootContext, @NotNull T> consumer) {
        return filtered((c, o) -> type.isInstance(o), (c, o) -> consumer.accept(c, (T) o));
    }

}

record FilteredImpl(@NotNull BiPredicate<@NotNull LootContext, @NotNull Object> predicate,
                    @NotNull BiConsumer<@NotNull LootContext, @NotNull Object> consumer) implements LootProcessor.Filtered {

    @Override
    public boolean canProcess(@NotNull LootContext context, @NotNull Object object) {
        return predicate.test(context, object);
    }

    @Override
    public void accept(@NotNull LootContext context, @NotNull Object object) {
        if (!canProcess(context, object)) throw new IllegalArgumentException("Object '" + object + "' did not fit the predicate!");
        consumer.accept(context, object);
    }
}
