package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.meta.LootConversionManager;
import dev.goldenstack.loot.generation.LootPool;
import dev.goldenstack.loot.generation.LootTable;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Stores information about how conversion of loot-related objects, such as tables and pools, should occur. Generally,
 * this should hold the basis for anything required to completely serialize and deserialize a loot table.
 * @param lootEntryManager the conversion manager for loot entries or subtypes of them
 * @param lootModifierManager the conversion manager for loot modifiers or subtypes of them
 * @param lootConditionManager the conversion manager for loot conditions or subtypes of them
 * @param lootNumberManager the conversion manager for loot numbers or subtypes of them
 * @param lootTableConverter the {@link LootConverter} for loot tables
 * @param lootPoolConverter the {@link LootConverter} for loot pools
 * @param nodeProducer the supplier used for creating default nodes. This is likely shorter than creating a node without
 *                     it, and it's also more configurable.
 * @param <L> the loot item type
 */
public record ImmuTables<L>(
        @NotNull LootConversionManager<L, LootEntry<L>> lootEntryManager,
        @NotNull LootConversionManager<L, LootModifier<L>> lootModifierManager,
        @NotNull LootConversionManager<L, LootCondition<L>> lootConditionManager,
        @NotNull LootConversionManager<L, LootNumber<L>> lootNumberManager,
        @NotNull LootConverter<L, LootTable<L>> lootTableConverter,
        @NotNull LootConverter<L, LootPool<L>> lootPoolConverter,
        @NotNull Supplier<ConfigurationNode> nodeProducer) {

    /**
     * Shortcut for {@code nodeProducer().get()} for convenience.
     * @return a new configuration node
     */
    public @NotNull ConfigurationNode createNode() {
        return nodeProducer().get();
    }

    /**
     * Creates a new builder for this class, with all builders unmodified and everything else as null.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new ImmuTables builder
     * @param <L> the loot item type
     */
    @Contract(" -> new")
    public static <L> @NotNull Builder<L> builder() {
        return new Builder<>();
    }

    public static final class Builder<L> {
        private final @NotNull LootConversionManager.Builder<L, LootEntry<L>> lootEntryBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootModifier<L>> lootModifierBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootCondition<L>> lootConditionBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootNumber<L>> lootNumberBuilder = LootConversionManager.builder();
        private Supplier<ConfigurationNode> nodeProducer;
        private LootConverter<L, LootTable<L>> lootTableConverter;
        private LootConverter<L, LootPool<L>> lootPoolConverter;

        private Builder() {}

        public @NotNull LootConversionManager.Builder<L, LootEntry<L>> lootEntryBuilder() {
            return lootEntryBuilder;
        }

        public @NotNull LootConversionManager.Builder<L, LootModifier<L>> lootModifierBuilder() {
            return lootModifierBuilder;
        }

        public @NotNull LootConversionManager.Builder<L, LootCondition<L>> lootConditionBuilder() {
            return lootConditionBuilder;
        }

        public @NotNull LootConversionManager.Builder<L, LootNumber<L>> lootNumberBuilder() {
            return lootNumberBuilder;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootEntryBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootEntry<L>>> builderConsumer) {
            builderConsumer.accept(this.lootEntryBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootModifierBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootModifier<L>>> builderConsumer) {
            builderConsumer.accept(this.lootModifierBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootConditionBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootCondition<L>>> builderConsumer) {
            builderConsumer.accept(this.lootConditionBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootNumberBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootNumber<L>>> builderConsumer) {
            builderConsumer.accept(this.lootNumberBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> nodeProducer(@NotNull Supplier<ConfigurationNode> nodeProducer) {
            this.nodeProducer = nodeProducer;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootTableConverter(@NotNull LootConverter<L, LootTable<L>> lootTableConverter) {
            this.lootTableConverter = lootTableConverter;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootPoolConverter(@NotNull LootConverter<L, LootPool<L>> lootPoolConverter) {
            this.lootPoolConverter = lootPoolConverter;
            return this;
        }

        @Contract(" -> new")
        public @NotNull ImmuTables<L> build() {
            Objects.requireNonNull(nodeProducer, "ImmuTables instances cannot be built without a node producer!");
            Objects.requireNonNull(lootTableConverter, "ImmuTables instances cannot be built without a loot table converter!");
            Objects.requireNonNull(lootPoolConverter, "ImmuTables instances cannot be built without a loot pool converter!");
            return new ImmuTables<>(
                    lootEntryBuilder.build(),
                    lootModifierBuilder.build(),
                    lootConditionBuilder.build(),
                    lootNumberBuilder.build(),
                    lootTableConverter,
                    lootPoolConverter,
                    nodeProducer
                );
        }
    }
}
