package dev.goldenstack.loot;

import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.json.JsonSerializationManager;
import dev.goldenstack.loot.provider.number.NumberProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Main class for serializing and deserializing loot tables
 */
public class LootTableLoader {

    private final @NotNull JsonSerializationManager<NumberProvider> numberProviderManager;
    private final @NotNull JsonSerializationManager<LootCondition> lootConditionManager;
    private LootTableLoader(@NotNull Builder builder){
        JsonSerializationManager.Builder<NumberProvider> numberProviderBuilder = JsonSerializationManager.builder();
        if (builder.numberProviderBuilder != null){
            builder.numberProviderBuilder.accept(numberProviderBuilder);
        }
        this.numberProviderManager = numberProviderBuilder.owner(this).build();
        JsonSerializationManager.Builder<LootCondition> lootConditionBuilder = JsonSerializationManager.builder();
        if (builder.lootConditionBuilder != null){
            builder.lootConditionBuilder.accept(lootConditionBuilder);
        }
        this.lootConditionManager = lootConditionBuilder.owner(this).build();
    }

    /**
     * Returns the JsonSerializationManager that is used to serialize/deserialize {@code NumberProvider}s
     */
    public @NotNull JsonSerializationManager<NumberProvider> getNumberProviderManager() {
        return numberProviderManager;
    }

    /**
     * Returns the JsonSerializationManager that is used to serialize/deserialize {@code LootCondition}s
     */
    public @NotNull JsonSerializationManager<LootCondition> getLootConditionManager() {
        return lootConditionManager;
    }

    /**
     * Creates a new {@link Builder}
     */
    public static @NotNull Builder builder(){
        return new Builder();
    }

    /**
     * Utility class for building {@code LootTableLoader} instances
     */
    public static class Builder {

        private Builder(){}

        private Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder;
        private Consumer<JsonSerializationManager.Builder<LootCondition>> lootConditionBuilder;

        /**
         * Sets the builder that is used for creating the {@link #getNumberProviderManager()}
         */
        @Contract("_ -> this")
        @NotNull Builder numberProviderBuilder(@NotNull Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder){
            this.numberProviderBuilder = numberProviderBuilder;
            return this;
        }

        /**
         * Sets the builder that is used for creating the {@link #getLootConditionManager()} ()}
         */
        @Contract("_ -> this")
        @NotNull Builder lootConditionBuilder(@NotNull Consumer<JsonSerializationManager.Builder<LootCondition>> lootConditionBuilder){
            this.lootConditionBuilder = lootConditionBuilder;
            return this;
        }

        /**
         * Builds a {@code LootTableLoader} instance from this builder.
         */
        public @NotNull LootTableLoader build(){
            return new LootTableLoader(this);
        }

    }
}
