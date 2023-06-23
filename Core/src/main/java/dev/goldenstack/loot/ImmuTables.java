package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.meta.LootConversionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Stores information about how conversion of loot-related objects, such as tables and pools, should occur. Generally,
 * this should hold the basis for anything required to completely serialize and deserialize a loot table.
 * @param converters the map of converters that this loader manages
 * @param nodeProducer the supplier used for creating default nodes. This is likely shorter than creating a node without
 *                     it, and it's also more configurable.
 */
public record ImmuTables(@NotNull List<LootConversionManager<?>> converters,
                         @NotNull Supplier<ConfigurationNode> nodeProducer) {

    public ImmuTables {
        Set<Type> types = new HashSet<>();
        for (var manager : converters) {
            if (!types.add(manager.baseType().getType())) {
                throw new IllegalArgumentException("Loader instance was provided multiple converters of type " + manager.baseType().getType());
            }
        }
    }

    /**
     * Shortcut for {@code nodeProducer().get()} for convenience.
     * @return a new configuration node
     */
    public @NotNull ConfigurationNode createNode() {
        return nodeProducer().get();
    }

    /**
     * Attempts to retrieve a manager that converts the provided type, returning null if there is not one.
     * @param type the type to convert
     * @return a valid converter, or null if there is not one
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable LootConversionManager<T> getConverter(@NotNull Type type) {
        for (var converter : converters) {
            if (converter.baseType().getType() == type) {
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
        throw new IllegalArgumentException("Could not find converter manager of type " + type);
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
     * @return a new ImmuTables builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final @NotNull Set<Type> addedTypes = new HashSet<>();
        private final @NotNull List<LootConversionManager<?>> managers = new ArrayList<>();
        private Supplier<ConfigurationNode> nodeProducer;

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder newBuilder(@NotNull LootConversionManager.Builder<?> builder) {
            var built = builder.build();
            if (addedTypes.contains(built.baseType().getType())) {
                throw new IllegalArgumentException("Cannot add a LootConversionManager of a type that is already present!");
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

        @Contract("_ -> this")
        public @NotNull Builder nodeProducer(@NotNull Supplier<ConfigurationNode> nodeProducer) {
            this.nodeProducer = nodeProducer;
            return this;
        }

        @Contract(" -> new")
        public @NotNull ImmuTables build() {
            return new ImmuTables(
                    List.copyOf(managers),
                    Objects.requireNonNull(nodeProducer, "ImmuTables instances cannot be built without a node producer!")
                );
        }
    }
}
