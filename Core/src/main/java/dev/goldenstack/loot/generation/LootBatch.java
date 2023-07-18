package dev.goldenstack.loot.generation;

import io.leangen.geantyref.GenericTypeReflector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Holds a collection of arbitrary items that will be handled by the various components of loot tables.
 * @param items the various items in this batch
 */
public record LootBatch(@NotNull List<?> items) {

    /**
     * Creates a batch containing the provided items.
     * @param items the items that the batch will hold
     * @return a new batch of the items
     */
    public static @NotNull LootBatch of(@NotNull List<?> items) {
        return new LootBatch(items);
    }

    /**
     * Creates a batch containing the provided items.
     * @param items the items that the batch will hold
     * @return a new batch of the items
     */
    public static @NotNull LootBatch of(@NotNull Object @NotNull ... items) {
        return new LootBatch(List.of(items));
    }

    /**
     * A batch containing zero items.
     */
    public static final @NotNull LootBatch EMPTY = new LootBatch(List.of());

    public LootBatch {
        items = List.copyOf(items);
    }

    /**
     * Modifies every item in this batch that is a subtype of the provided type, according to the provided mapper.
     * @param superType the supertype that acts as a filter
     * @param mapper the mapper function. Items will be removed if this mapper returns null.
     * @return a new batch containing the modified items
     * @param <T> the type of object to modify. This should be equal to the type argument.
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull LootBatch modify(@NotNull Type superType, @NotNull Function<T, Object> mapper) {
        List<Object> filtered = new ArrayList<>();
        for (Object item : items()) {
            if (GenericTypeReflector.isSuperType(superType, item.getClass())) {
                // Casting should be safe as we verified the type
                item = mapper.apply((T) item);
            }

            if (item != null) {
                filtered.add(item);
            }
        }

        return LootBatch.of(filtered);
    }

    /**
     * Identical to {@link #modify(Type, Function)} except that it allows the client to provide a class so the type
     * argument can be inferred.
     * @see #modify(Type, Function)
     */
    public <T> @NotNull LootBatch modify(@NotNull Class<T> superType, @NotNull Function<T, Object> mapper) {
        return modify((Type) superType, mapper);
    }

}
