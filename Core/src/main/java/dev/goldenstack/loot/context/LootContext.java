package dev.goldenstack.loot.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Stores information about some loot table that is being queried for loot items
 * @param random the random number generator that will be used, potentially by actual loot modifiers, requirements,
 *               entries, numbers, or other sources
 * @param information the map that stores all extra information about this context
 */
public record LootContext(@NotNull Random random, @NotNull Map<Key<?>, Object> information) {

    public LootContext {
        information = Map.copyOf(information);
    }

    /**
     * @return the object stored at the specified key
     */
    public @Nullable Object getRaw(@NotNull Key<?> key) {
        return this.information.get(key);
    }

    /**
     * Gets the object stored with the provided key, returns null if there isn't an object, throws a
     * {@link ClassCastException} if there is but it's a different type, and otherwise returns the actual object.
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(@NotNull Key<T> key) {
        Object object = this.information.get(key);
        if (object == null) {
            return null;
        }
        return (T) object;
    }

    /**
     * Gets the object stored with the provided key, throws a {@link NoSuchElementException} if there isn't an object,
     * throws a {@link ClassCastException} if there is but it's a different type, and otherwise returns the actual
     * object.
     */
    @SuppressWarnings("unchecked")
    public @NotNull <T> T assure(@NotNull Key<T> key) {
        Object object = this.information.get(key);
        if (object == null) {
            throw new NoSuchElementException("Value for key \"" + key + "\" could not be found while reading a loot table");
        }
        return (T) object;
    }

    /**
     * @return a new LootContext builder
     */
    @Contract(" -> new")
    public @NotNull Builder toBuilder() {
        return builder().random(this.random).addInformation(this.information);
    }

    /**
     * Note: builders of this class are not thread-safe.
     * @return a new builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Random random;
        private final @NotNull Map<Key<?>, Object> information = new HashMap<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder random(@NotNull Random random) {
            this.random = random;
            return this;
        }

        @Contract("_, _ -> this")
        public <T> @NotNull Builder addInformation(@NotNull Key<T> key, @NotNull T information) {
            this.information.put(key, information);
            return this;
        }

        @Contract("_ -> this")
        public <T> @NotNull Builder addInformation(@NotNull Map<Key<?>, Object> information) {
            this.information.putAll(information);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootContext build() {
            Objects.requireNonNull(random, "Loot context instances cannot be built without a random number generator");
            return new LootContext(this.random, this.information);
        }
    }

    /**
     * Used to grab data from a LootContext
     * @param <T> the type of the object that this key represents
     */
    public record Key<T>(@NotNull String key) {}
}
