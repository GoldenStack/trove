package dev.goldenstack.loot.context;

import dev.goldenstack.loot.Trove;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores necessary information when converting (i.e., serializing or deserializing) something related to loot tables.
 * The {@link Trove} instance is stored on its own because the vast majority of times when something loot-related
 * is being converted will require usage of it.
 * @param loader the loader to be used
 * @param information the context's internal information
 */
public record LootConversionContext(@NotNull Trove loader, @NotNull Map<Key<?>, Object> information) implements LootContext {

    public LootConversionContext {
        information = Map.copyOf(information);
    }

    /**
     * Creates a new builder for this class, with no information and a null loader.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new LootConversionContext builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final @NotNull Map<Key<?>, Object> information = new HashMap<>();
        private Trove loader;

        private Builder() {}

        @Contract("_, _ -> this")
        public <T> @NotNull Builder addInformation(@NotNull Key<T> key, @NotNull T value) {
            information.put(key, value);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder loader(@NotNull Trove loader) {
            this.loader = loader;
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootConversionContext build() {
            return new LootConversionContext(
                    Objects.requireNonNull(loader, "LootConversionContext instances cannot be built without a loader!"),
                    information
            );
        }
    }
}

