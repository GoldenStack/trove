package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.meta.LootConversionManager;
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
 * @param nodeProducer the supplier used for creating default nodes. This is likely shorter than creating a node without
 *                     it, and it's also more configurable.
 * @param <L> the loot item type
 */
public record ImmuTables<L>(
        @NotNull LootConversionManager<L, LootEntry<L>> lootEntryManager,
        @NotNull LootConversionManager<L, LootModifier<L>> lootModifierManager,
        @NotNull LootConversionManager<L, LootCondition<L>> lootConditionManager,
        @NotNull LootConversionManager<L, LootNumber<L>> lootNumberManager,
        @NotNull Supplier<ConfigurationNode> nodeProducer) {

    /**
     * Shortcut for {@code nodeProducer().get()} for convenience.
     * @return a new configuration node
     */
    public @NotNull ConfigurationNode createNode() {
        return nodeProducer().get();
    }

    /**
     * Creates a new builder for this class, with no information and a null loader.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new ImmuTables builder
     * @param <L> the loot item type
     */
    @Contract(" -> new")
    public static <L> @NotNull Builder<L> builder() {
        return new Builder<>();
    }

    public static final class Builder<L> {
        private Supplier<ConfigurationNode> nodeProducer;

        private final @NotNull LootConversionManager.Builder<L, LootEntry<L>> lootEntryBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootModifier<L>> lootModifierBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootCondition<L>> lootConditionBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootNumber<L>> lootNumberBuilder = LootConversionManager.builder();

        private Builder() {}

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

        @Contract(" -> new")
        public @NotNull ImmuTables<L> build() {
            Objects.requireNonNull(nodeProducer, "ImmuTables instances cannot be built without a node producer!");
            return new ImmuTables<>(
                    lootEntryBuilder.build(),
                    lootModifierBuilder.build(),
                    lootConditionBuilder.build(),
                    lootNumberBuilder.build(),
                    nodeProducer
                );
        }
    }
}
