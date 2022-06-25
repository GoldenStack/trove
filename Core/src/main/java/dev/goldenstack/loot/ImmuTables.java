package dev.goldenstack.loot;

import dev.goldenstack.loot.json.LootConversionManager;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.structure.LootRequirement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Stores information about how to serialize and deserialize loot tables
 * @param <L> the loot item that gets generated
 */
public class ImmuTables<L> {

    private final @NotNull LootConversionManager<L, LootEntry<L>> lootEntryManager;
    private final @NotNull LootConversionManager<L, LootModifier<L>> lootModifierManager;
    private final @NotNull LootConversionManager<L, LootRequirement<L>> lootRequirementManager;
    private final @NotNull LootConversionManager<L, LootNumber<L>> lootNumberManager;

    private ImmuTables(@NotNull Builder<L> builder) {
        this.lootEntryManager = builder.lootEntryBuilder.owner(this).build();
        this.lootModifierManager = builder.lootModifierBuilder.owner(this).build();
        this.lootRequirementManager = builder.lootRequirementBuilder.owner(this).build();
        this.lootNumberManager = builder.lootNumberBuilder.owner(this).build();
    }

    /**
     * @return the LootConversionManager that handles loot entries
     */
    public @NotNull LootConversionManager<L, LootEntry<L>> lootEntryManager() {
        return lootEntryManager;
    }

    /**
     * @return the LootConversionManager that handles loot modifiers
     */
    public @NotNull LootConversionManager<L, LootModifier<L>> lootModifierManager() {
        return lootModifierManager;
    }

    /**
     * @return the LootConversionManager that handles loot requirements
     */
    public @NotNull LootConversionManager<L, LootRequirement<L>> lootRequirementManager() {
        return lootRequirementManager;
    }

    /**
     * @return the LootConversionManager that handles loot numbers
     */
    public @NotNull LootConversionManager<L, LootNumber<L>> lootNumberManager() {
        return lootNumberManager;
    }

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

        private Builder() {}

        public @NotNull LootConversionManager.Builder<L, LootEntry<L>> lootEntryBuilder() {
            return lootEntryBuilder;
        }

        public @NotNull LootConversionManager.Builder<L, LootModifier<L>> lootModifierBuilder() {
            return lootModifierBuilder;
        }

        public @NotNull LootConversionManager.Builder<L, LootRequirement<L>> lootRequirementBuilder() {
            return lootRequirementBuilder;
        }

        public @NotNull LootConversionManager.Builder<L, LootNumber<L>> lootNumberBuilder() {
            return lootNumberBuilder;
        }

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

        @Contract(" -> new")
        public @NotNull ImmuTables<L> build() {
            return new ImmuTables<>(this);
        }
    }

}
