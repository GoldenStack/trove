package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.meta.LootConversionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Stores information about how conversion of loot-related objects, such as tables and pools, should occur. Generally,
 * this should hold the basis for anything required to completely serialize and deserialize a loot table.
 * @param converters the map of converters that this loader manages
 */
public record Trove(@NotNull List<LootConversionManager<?>> converters) {

    public Trove {
        Set<Type> types = new HashSet<>();
        for (var manager : converters) {
            if (!types.add(manager.baseType().getType())) {
                throw new IllegalArgumentException("Cannot load multiple converters of type '" + manager.baseType().getType() + "'");
            }
        }
    }

    /**
     * Attempts to retrieve a manager that converts the provided type, returning null if there is not one.
     * @param type the type to convert
     * @return a valid converter, or null if there is not one
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable LootConversionManager<T> getConverter(@NotNull Type type) {
        for (var converter : converters) {
            if (converter.baseType().getType().equals(type)) {
                // This is safe as their types must be identical
                return (LootConversionManager<T>) converter;
            }
        }
        return null;
    }

    /**
     * Returns a valid manager that converts the provided type, throwing an exception if there is not one.
     * @param type the type to convert
     * @return a valid manager
     */
    public <T> @NotNull LootConversionManager<T> requireConverter(@NotNull Type type) {
        var get = this.<T>getConverter(type);
        if (get != null) {
            return get;
        }
        throw new IllegalArgumentException("Unknown converter type '" + type + "'");
    }

    /**
     * Returns a valid manager that converts the provided type, throwing an exception if there is not one.
     * @param type the type to convert
     * @return a valid manager
     */
    public <T> @NotNull LootConversionManager<T> requireConverter(@NotNull Class<T> type) {
        return requireConverter((Type) type);
    }

    /**
     * Creates a new builder for this class, with all builders unmodified and everything else as null.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new Trove builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final @NotNull Set<Type> addedTypes = new HashSet<>();
        private final @NotNull List<LootConversionManager<?>> managers = new ArrayList<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder newBuilder(@NotNull LootConversionManager.Builder<?> builder) {
            var built = builder.build();
            if (addedTypes.contains(built.baseType().getType())) {
                throw new IllegalArgumentException("Type '" + built.baseType().getType() + "' is already present in this builder");
            }
            this.managers.add(built);
            this.addedTypes.add(built.baseType().getType());
            return this;
        }

        @Contract("_ -> this")
        public <T> @NotNull Builder newBuilder(@NotNull Consumer<LootConversionManager.Builder<T>> builderConsumer) {
            LootConversionManager.Builder<T> builder = LootConversionManager.builder();
            builderConsumer.accept(builder);
            return newBuilder(builder);
        }

        @Contract(" -> new")
        public @NotNull Trove build() {
            return new Trove(List.copyOf(managers));
        }
    }
}