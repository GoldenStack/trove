package dev.goldenstack.loot.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A simple context object to store information about loot being generated. The random number generator is stored
 * separately from the map because the overwhelming majority of times when loot is being generated require a random
 * number generator, and so doing it this way makes it easier to assure that it exists while also faster and easier to
 * access.
 * @param random the random number generator to be used
 * @param information the context's internal information
 */
public record LootGenerationContext(@NotNull Random random, @NotNull Map<Key<?>, Object> information) implements LootContext {

    public LootGenerationContext {
        information = Map.copyOf(information);
    }

    /**
     * Creates a new builder for this class, with no information and a null random number generator.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new LootGenerationContext builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Random random;
        private final @NotNull Map<Key<?>, Object> information = new HashMap<>();

        private Builder() {}

        @Contract("_, _ -> this")
        public <T> @NotNull Builder addInformation(@NotNull Key<T> key, @NotNull T value) {
            information.put(key, value);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder random(@NotNull Random random) {
            this.random = random;
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootGenerationContext build() {
            return new LootGenerationContext(
                    Objects.requireNonNull(random, "This builder cannot be built without a random number generator"),
                    information
            );
        }
    }
}
