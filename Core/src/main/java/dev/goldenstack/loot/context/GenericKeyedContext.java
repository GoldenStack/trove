package dev.goldenstack.loot.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.NoSuchElementException;

sealed interface GenericKeyedContext<K> permits LootContext, LootConversionContext {

    /**
     * Note: The map returned will always be fully immutable
     * @return an immutable map of this context's information
     */
    @Unmodifiable
    @Contract(pure = true)
    @NotNull Map<K, Object> information();

    /**
     * @param key the key to search for
     * @return true if there is something stored at the provided key
     */
    @Contract(pure = true)
    default boolean has(@NotNull K key) {
        return information().containsKey(key);
    }

    /**
     * @param key the key to get the (raw) object from
     * @return the object stored at the specified key
     */
    @Contract(pure = true)
    default @Nullable Object getRaw(@NotNull K key) {
        return information().get(key);
    }

    /**
     * Gets the object stored with the provided key, returns null if there isn't an object, throws a
     * {@link ClassCastException} if there is, but it's a different type, and otherwise returns the actual object.
     * @param key the key to get the object from
     * @return the object at the provided key if it is an instance of {@link T} or null (or an error) if not applicable
     * @param <T> the generic type of the key
     */
    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    default <T> @Nullable T get(@NotNull K key) {
        Object object = information().get(key);
        if (object == null) {
            return null;
        }
        return (T) object;
    }

    /**
     * Gets the object stored with the provided key, throws a {@link NoSuchElementException} if there isn't an object,
     * throws a {@link ClassCastException} if there is, but it's a different type, and otherwise returns the actual
     * object.
     * @param key the key to get the object from
     * @return the object at the provided key if it is an instance of {@link T}, or an exception if not
     * @param <T> the generic type of the key
     */
    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    default @NotNull <T> T assure(@NotNull K key) {
        Object object = information().get(key);
        if (object == null) {
            throw new NoSuchElementException("Value for key \"" + key + "\" could not be found");
        }
        return (T) object;
    }

}
