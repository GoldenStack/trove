package dev.goldenstack.loot;

import dev.goldenstack.loot.conversion.LootConversionManager;
import dev.goldenstack.loot.structure.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Stores information about how to serialize and deserialize loot tables
 * @param <L> the loot item that gets generated
 */
public record ImmuTables<L>(@NotNull LootConversionManager<L, LootEntry<L>> lootEntryManager,
                            @NotNull LootConversionManager<L, LootModifier<L>> lootModifierManager,
                            @NotNull LootConversionManager<L, LootRequirement<L>> lootRequirementManager,
                            @NotNull LootConversionManager<L, LootNumber<L>> lootNumberManager,
                            @NotNull LootTable.Converter<L> lootTableConverter,
                            @NotNull LootPool.Converter<L> lootPoolConverter,
                            @NotNull Supplier<ConfigurationNode> nodeProducer) {

    public @NotNull ConfigurationNode createNode() {
        return nodeProducer.get();
    }

    /**
     * @return a new ImmuTables builder
     * @param <L> the loot item
     */
    @Contract(" -> new")
    public static <L> @NotNull Builder<L> builder() {
        return new Builder<>();
    }

    /**
     * Utility class for creating {@link ImmuTables} instances.
     * @param <L> the loot item
     */
    public static final class Builder<L> {

        private final @NotNull LootConversionManager.Builder<L, LootEntry<L>> lootEntryBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootModifier<L>> lootModifierBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootRequirement<L>> lootRequirementBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootNumber<L>> lootNumberBuilder = LootConversionManager.builder();

        private LootTable.Converter<L> lootTableConverter;
        private LootPool.Converter<L> lootPoolConverter;

        private Supplier<ConfigurationNode> nodeProducer;

        private Builder() {}

        /**
         * @param consumer the consumer that will be fed this builder's loot entry builder
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> lootEntryBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootEntry<L>>> consumer) {
            consumer.accept(this.lootEntryBuilder);
            return this;
        }

        /**
         * @param consumer the consumer that will be fed this builder's loot modifier builder
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> lootModifierBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootModifier<L>>> consumer) {
            consumer.accept(this.lootModifierBuilder);
            return this;
        }

        /**
         * @param consumer the consumer that will be fed this builder's loot requirement builder
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> lootRequirementBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootRequirement<L>>> consumer) {
            consumer.accept(this.lootRequirementBuilder);
            return this;
        }

        /**
         * @param consumer the consumer that will be fed this builder's loot number builder
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> lootNumberBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootNumber<L>>> consumer) {
            consumer.accept(this.lootNumberBuilder);
            return this;
        }

        /**
         * @param lootTableConverter the converter that will be relied upon to convert loot tables
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> lootTableConverter(@NotNull LootTable.Converter<L> lootTableConverter) {
            this.lootTableConverter = lootTableConverter;
            return this;
        }

        /**
         * @param lootPoolConverter the converter that will be relied upon to convert loot pools
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> lootPoolConverter(@NotNull LootPool.Converter<L> lootPoolConverter) {
            this.lootPoolConverter = lootPoolConverter;
            return this;
        }

        /**
         * @param nodeProducer the node producer that converters may use
         * @return this (for chaining)
         */
        @Contract("_ -> this")
        public @NotNull Builder<L> nodeProducer(@NotNull Supplier<ConfigurationNode> nodeProducer) {
            this.nodeProducer = nodeProducer;
            return this;
        }

        /**
         * Note: it is safe to build this builder multiple times, but it is not recommended to do so.
         * @return a new {@code ImmuTables<L>} instance created from this builder.
         */
        @Contract(" -> new")
        public @NotNull ImmuTables<L> build() {
            Objects.requireNonNull(lootTableConverter, "ImmuTables instances cannot be built without a loot table converter!");
            Objects.requireNonNull(lootPoolConverter, "ImmuTables instances cannot be built without a loot pool converter!");
            Objects.requireNonNull(nodeProducer, "ImmuTables instances cannot be built without a node producer!");
            return new ImmuTables<>(
                lootEntryBuilder.build(),
                lootModifierBuilder.build(),
                lootRequirementBuilder.build(),
                lootNumberBuilder.build(),
                lootTableConverter,
                lootPoolConverter,
                nodeProducer
            );
        }
    }

}
