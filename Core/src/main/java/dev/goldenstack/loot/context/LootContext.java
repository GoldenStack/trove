package dev.goldenstack.loot.context;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A context interface, meant as a base for when some commonly made operation needs to handle customizable context in
 * its input. This is done with a simple map, but with keys that hold their own {@link TypeToken} so that their types
 * can be verified during runtime.<br>
 * Essentially, this basic interface is a type-safe immutable map wrapper with a few convenience methods while still
 * allowing access to the internal (and also immutable) map.
 */
public interface LootContext {

    /**
     * A simple way to store a key along with its type. Generally, instances of this should be stored and not created on
     * demand.<br>
     * Note: this class's {@link #equals(Object)} and {@link #hashCode()} method do not take each instance's type token
     * into account, as they would make the token do something other than act as a passive marker of the type that can
     * be used when needed. Be aware of that when using this class.<br>
     * Additionally, an object can be stored by this key if it is a subtype of {@link T} - exact equality of classes is
     * not checked. For specificity, all internal comparison is done with
     * {@link GenericTypeReflector#isSuperType(Type, Type)}.
     * @param key the string key of this object
     * @param token the type token, storing, during runtime, the exact type of value that this key can work with
     * @param <T> this key's type, representing which type of objects can be set as its value when entered into a
     *            LootContext
     */
    record Key<T>(@NotNull String key, @NotNull TypeToken<T> token) {
        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof Key<?> k && k.key.equals(key));
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    /**
     * Acquires this context's immutable and internal map of information. This should generally not be used, as the
     * methods in this class itself are likely enough and are, importantly, safe, but it's open for usage if needed.
     * @return the immutable map of keys to their values
     */
    @Unmodifiable
    @Contract(pure = true)
    @NotNull Map<Key<?>, Object> information();

    /**
     * Checks if a value that is equal to or is a subtype of the provided key's type. This could return null for one of
     * two reasons: first, if there is not a value present at the key's string, and second, if there is a value at the
     * key, but it's not a subtype of (or equal to) the key's type. See the documentation of {@link Key} for more
     * specific information.
     * @param key the key to check for
     * @return true if this context has the key and the value's type is applicable with the type of the provided key,
     *         and false if otherwise
     */
    @Contract(pure = true)
    default boolean has(@NotNull Key<?> key) {
        return get(key) != null;
    }

    /**
     * Gets the value from this map associated with the provided key if its type is a supertype of or is equal to the
     * provided key's type, and returns null if there wasn't one. Check {@link #has(Key)} for more specific information
     * on what this entails.
     * @param key the key, to attempt to retrieve the value from
     * @return the instance of {@link T} that is associated with the provided key, or null if there was not one
     * @param <T> the type of the key to search for values of
     */
    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    default <T> @Nullable T get(@NotNull Key<T> key) {
        Object object = information().get(key);
        if (object != null && GenericTypeReflector.isSuperType(key.token().getType(), object.getClass())) {
            return (T) object;
        }
        return null;
    }

    /**
     * Assures that there is a value, of a type that works with the key's type, stored at the provided key. Refer to
     * {@link #get(Key)} and {@link #has(Key)} for more specific information.
     * @param key the key, to attempt to retrieve the value from
     * @return the instance of {@link T} that is associated with the provided key
     * @param <T> the type of the key to search for values of
     * @throws NoSuchElementException if there is no value stored at the provided key or if it is of an invalid type.
     *                                Note: the error thrown will specify whether it was an invalid type or if it was
     *                                just null.
     */
    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    default @NotNull <T> T assure(@NotNull Key<T> key) {
        Object object = information().get(key);
        if (object == null) {
            throw new NoSuchElementException("Value for key \"" + key + "\" could not be found");
        } else if (!GenericTypeReflector.isSuperType(key.token().getType(), object.getClass())) {
            throw new NoSuchElementException("Value for key \"" + key + "\" could not be retrieved as it is of an" +
                    "incorrect type (expected: " + key.token.getType() + ", found: " + object.getClass() + ")");
        }
        return (T) object;
    }

}
