package dev.goldenstack.loot.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Stores information about which loot context keys are required for generation.
 */
public record LootContextCriterion(@NotNull String key, @NotNull Set<LootContext.Key<?>> required) {

    public LootContextCriterion {
        Objects.requireNonNull(key, "Loot context criterion instances must have a key!");
        required = Set.copyOf(required);
    }

    /**
     * @param key the key to check against
     * @return true if the provided key is required according to {@link #required()}, otherwise false
     */
    public boolean isRequired(@NotNull LootContext.Key<?> key) {
        return required.contains(key);
    }

    /**
     * @param context the LootContext object to verify
     * @return true if all of the required keys in this criterion are fulfilled in the provided context, otherwise false
     */
    public boolean fulfills(@NotNull LootContext context) {
        for (var contextKey : required) {
            if (context.getRaw(contextKey) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return a new LootContextCriterion builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Utility class for creating {@link LootContextCriterion} instances.
     */
    public static final class Builder {
        private String key;
        private final @NotNull Set<LootContext.Key<?>> required = new HashSet<>();

        private Builder() {}

        /**
         * @param key the key of the built criterion
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder key(@NotNull String key) {
            this.key = key;
            return this;
        }

        /**
         * @param key a loot context key to require
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder require(@NotNull LootContext.Key<?> key) {
            this.required.add(key);
            return this;
        }

        /**
         * @param keys the loot context keys to require
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder require(@NotNull Set<LootContext.Key<?>> keys) {
            this.required.addAll(keys);
            return this;
        }

        /**
         * Note: it is safe to build this builder multiple times, but it is not recommended to do so.
         * @return a new {@code LootContextCriterion} instance created from this builder.
         */
        @Contract(" -> new")
        public @NotNull LootContextCriterion build() {
            return new LootContextCriterion(this.key, this.required);
        }
    }
}
