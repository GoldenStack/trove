package dev.goldenstack.loot.context;

import dev.goldenstack.loot.ImmuTables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores information meant to be used during conversion.
 * @param loader the loader - it's used in most cases, so it's being stored here instead of in {@code information}.
 * @param information the map that stores possibly required information
 * @param <L> the loot item
 */
public record LootConversionContext<L>(@NotNull ImmuTables<L> loader, @NotNull Map<Key<?>, Object> information) implements GenericKeyedContext<LootConversionContext.Key<?>> {

    public LootConversionContext {
        information = Map.copyOf(information);
    }

    /**
     * Note: the returned builder is not thread-safe.
     * @return a new builder
     * @param <L> the loot item
     */
    @Contract(" -> new")
    public static <L> @NotNull Builder<L> builder() {
        return new Builder<>();
    }

    /**
     * Utility class for creating {@link LootConversionContext} instances.
     * @param <L> the loot item
     */
    public static final class Builder<L> {
        private ImmuTables<L> loader;
        private final @NotNull Map<Key<?>, Object> information = new HashMap<>();

        private Builder() {}

        /**
         * @param loader the {@link ImmuTables<L>} instance that will be used for conversion
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> loader(@NotNull ImmuTables<L> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * @param key the key of the information
         * @param information the actual information that will be stored
         * @return this (for chaining)
         * @param <T> the class of the key
         */
        @Contract("_, _ -> this")
        public <T> @NotNull Builder<L> addInformation(@NotNull Key<T> key, @NotNull T information) {
            this.information.put(key, information);
            return this;
        }

        /**
         * Note: it is safe to build this builder multiple times, but it is not recommended to do so.
         * @return a new {@code LootConversionContext<L>} instance created from this builder.
         */
        @Contract(" -> new")
        public @NotNull LootConversionContext<L> build() {
            Objects.requireNonNull(loader, "Loot conversion context instances cannot be built without a loader");
            return new LootConversionContext<>(this.loader, this.information);
        }
    }

    /**
     * Used to grab data from a LootConversionContext
     * @param <T> the type of the object that this key represents
     */
    public record Key<T>(@NotNull String key) {}
}
