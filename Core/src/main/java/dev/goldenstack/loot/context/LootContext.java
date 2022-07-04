package dev.goldenstack.loot.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Stores information about some loot table that is being queried for loot items
 * @param random the random number generator that will be used, potentially by actual loot modifiers, requirements,
 *               entries, numbers, or other sources
 * @param information the map that stores all extra information about this context
 */
public record LootContext(@NotNull Random random, @NotNull Map<Key<?>, Object> information) implements GenericKeyedContext<LootContext.Key<?>> {

    public LootContext {
        information = Map.copyOf(information);
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
