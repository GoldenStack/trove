package dev.goldenstack.loot.context;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Stores information that may be relevant during the generation of loot.
 */
public sealed interface LootContext permits LootContextImpl {

    /**
     * Represents some abstract key in some loot context. This will not
     * @param name the name of this key that values will be stored under
     * @param type the token storing the type of this key
     * @param <T> the type of this key
     */
    record Key<T>(@NotNull String name, @NotNull TypeToken<T> type) {}

    /**
     * Returns this context's {@link Random} instance.
     * @return this context's random instance
     */
    @NotNull Random random();

    /**
     * Returns whether or not this context has the provided key.
     * @param key the key to search for
     * @return true if this context has the key, false if not
     */
    boolean has(@NotNull Key<?> key);

    /**
     * Gets the object associated with the provided key's name if it is of the key's type, returning null if not.
     * @param key the key to search for
     * @return the object associated with the provided key, or null if there is not one
     * @param <T> the type of object desired
     */
    <T> @Nullable T get(@NotNull Key<T> key);

    /**
     * Gets the object associated with the provided key's name if it is of the key's type, returning the default value
     * if not.
     * @param key the key to search for
     * @param defaultValue the default value to use
     * @return the object associated with the provided key, or the default value if there is not one
     * @param <T> the type of object desired
     */
    <T> @NotNull T get(@NotNull Key<T> key, @NotNull T defaultValue);

    /**
     * Gets the object associated with the provided key's name if it is of the key's type, throwing an exception if not.
     * @param key the key to search for
     * @return the object associated with the provided key
     * @param <T> the type of object desired
     */
    <T> @NotNull T assure(@NotNull Key<T> key);

    /**
     * Creates a new LootContext builder.
     * @return a new builder
     */
    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {

        private Random random;
        private final Map<String, Object> information = new HashMap<>();

        private Builder() {}

        /**
         * Sets the random instance that will be used when this builder is built.
         * @param random the random instance
         * @return this, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder random(@NotNull Random random) {
            this.random = random;
            return this;
        }

        /**
         * Stores the value under the provided key when this builder is built.
         * @param key the key to store the value under
         * @param value the value to be stored
         * @return this, for chaining
         * @param <T> the type of the key being stored
         */
        @Contract("_, _ -> this")
        public <T> @NotNull Builder with(@NotNull Key<T> key, @NotNull T value) {
            information.put(key.name(), value);
            return this;
        }

        /**
         * Builds this builder into a new LootContext instance.
         * @return the new loot context
         */
        @Contract(" -> new")
        public @NotNull LootContext build() {
            return new LootContextImpl(
                    Objects.requireNonNull(random, "This builder cannot be built without a random number generator"),
                    information
            );
        }

    }

}

record LootContextImpl(@NotNull Random random, @NotNull Map<String, Object> information) implements LootContext {

    LootContextImpl {
        information = Map.copyOf(information);
    }

    @Override
    public boolean has(@NotNull Key<?> key) {
        return get(key) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T get(@NotNull Key<T> key) {
        var object = information.get(key.name());
        if (object != null && GenericTypeReflector.isSuperType(key.type().getType(), object.getClass())) {
            return (T) object;
        }
        return null;
    }

    @Override
    public <T> @NotNull T get(@NotNull Key<T> key, @NotNull T defaultValue) {
        var get = get(key);
        return get != null ? get : defaultValue;
    }

    @Override
    public <T> @NotNull T assure(@NotNull Key<T> key) {
        var get = get(key);
        if (get != null) {
            return get;
        }
        throw new NoSuchElementException("No value for key '" + key + "' with type '" + key.type().getType() + "'");
    }
}
