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
 * @param lootEntryManager the conversion manager for loot entries or subtypes of them
 * @param lootModifierManager the conversion manager for loot modifiers or subtypes of them
 * @param lootConditionManager the conversion manager for loot conditions or subtypes of them
 * @param lootNumberManager the conversion manager for loot numbers or subtypes of them
 * @param nodeProducer the supplier used for creating default nodes. This is likely shorter than creating a node without
 *                     it, and it's also more configurable.
 */
public record ImmuTables(@NotNull LootConversionManager<LootEntry> lootEntryManager,
                         @NotNull LootConversionManager<LootModifier> lootModifierManager,
                         @NotNull LootConversionManager<LootCondition> lootConditionManager,
                         @NotNull LootConversionManager<LootNumber> lootNumberManager,
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
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final @NotNull LootConversionManager.Builder<LootEntry> lootEntryBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<LootModifier> lootModifierBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<LootCondition> lootConditionBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<LootNumber> lootNumberBuilder = LootConversionManager.builder();
        private Supplier<ConfigurationNode> nodeProducer;

        private Builder() {}

        public @NotNull LootConversionManager.Builder<LootEntry> lootEntryBuilder() {
            return lootEntryBuilder;
        }

        public @NotNull LootConversionManager.Builder<LootModifier> lootModifierBuilder() {
            return lootModifierBuilder;
        }

        public @NotNull LootConversionManager.Builder<LootCondition> lootConditionBuilder() {
            return lootConditionBuilder;
        }

        public @NotNull LootConversionManager.Builder<LootNumber> lootNumberBuilder() {
            return lootNumberBuilder;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootEntryBuilder(@NotNull Consumer<LootConversionManager.Builder<LootEntry>> builderConsumer) {
            builderConsumer.accept(this.lootEntryBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootModifierBuilder(@NotNull Consumer<LootConversionManager.Builder<LootModifier>> builderConsumer) {
            builderConsumer.accept(this.lootModifierBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootConditionBuilder(@NotNull Consumer<LootConversionManager.Builder<LootCondition>> builderConsumer) {
            builderConsumer.accept(this.lootConditionBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootNumberBuilder(@NotNull Consumer<LootConversionManager.Builder<LootNumber>> builderConsumer) {
            builderConsumer.accept(this.lootNumberBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder nodeProducer(@NotNull Supplier<ConfigurationNode> nodeProducer) {
            this.nodeProducer = nodeProducer;
            return this;
        }

        @Contract(" -> new")
        public @NotNull ImmuTables build() {
            return new ImmuTables(
                    lootEntryBuilder.build(),
                    lootModifierBuilder.build(),
                    lootConditionBuilder.build(),
                    lootNumberBuilder.build(),
                    Objects.requireNonNull(nodeProducer, "ImmuTables instances cannot be built without a node producer!")
                );
        }
    }
}
