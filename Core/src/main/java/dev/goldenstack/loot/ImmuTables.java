package dev.goldenstack.loot;

import dev.goldenstack.loot.conversion.LootConversionManager;
import dev.goldenstack.loot.structure.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Stores information about how to serialize and deserialize loot tables
 * @param <L> the loot item that gets generated
 */
public record ImmuTables<L>(@NotNull LootConversionManager<L, LootEntry<L>> lootEntryManager,
                            @NotNull LootConversionManager<L, LootModifier<L>> lootModifierManager,
                            @NotNull LootConversionManager<L, LootRequirement<L>> lootRequirementManager,
                            @NotNull LootConversionManager<L, LootNumber<L>> lootNumberManager,
                            @NotNull LootTable.Converter<L> lootTableConverter,
                            @NotNull LootPool.Converter<L> lootPoolConverter) {

    /**
     * @return a new ImmuTables builder
     * @param <L> the loot item
     */
    @Contract(" -> new")
    public static <L> @NotNull Builder<L> builder() {
        return new Builder<>();
    }

    public static final class Builder<L> {

        private final @NotNull LootConversionManager.Builder<L, LootEntry<L>> lootEntryBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootModifier<L>> lootModifierBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootRequirement<L>> lootRequirementBuilder = LootConversionManager.builder();
        private final @NotNull LootConversionManager.Builder<L, LootNumber<L>> lootNumberBuilder = LootConversionManager.builder();

        private LootTable.Converter<L> lootTableConverter;
        private LootPool.Converter<L> lootPoolConverter;

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder<L> lootEntryBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootEntry<L>>> consumer) {
            consumer.accept(this.lootEntryBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootModifierBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootModifier<L>>> consumer) {
            consumer.accept(this.lootModifierBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootRequirementBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootRequirement<L>>> consumer) {
            consumer.accept(this.lootRequirementBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootNumberBuilder(@NotNull Consumer<LootConversionManager.Builder<L, LootNumber<L>>> consumer) {
            consumer.accept(this.lootNumberBuilder);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootTableConverter(@NotNull LootTable.Converter<L> lootTableConverter) {
            this.lootTableConverter = lootTableConverter;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder<L> lootPoolConverter(@NotNull LootPool.Converter<L> lootPoolConverter) {
            this.lootPoolConverter = lootPoolConverter;
            return this;
        }

        @Contract(" -> new")
        public @NotNull ImmuTables<L> build() {
            Objects.requireNonNull(lootTableConverter, "ImmuTables instances cannot be built without a loot table converter!");
            Objects.requireNonNull(lootPoolConverter, "ImmuTables instances cannot be built without a loot pool converter!");
            return new ImmuTables<>(
                lootEntryBuilder.build(),
                lootModifierBuilder.build(),
                lootRequirementBuilder.build(),
                lootNumberBuilder.build(),
                lootTableConverter,
                lootPoolConverter
            );
        }
    }

}
