package dev.goldenstack.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.json.JsonSerializationManager;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.util.NumberRange;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Utility method for deserializing number ranges. This exists because the default NumberRange deserialization
     * method, {@link NumberRange#deserialize(LootTableLoader, JsonElement, String)}, does not fit for the standard
     * {@code BiFunction<JsonElement, String, T>} that some methods in this library use.
     */
    public @NotNull NumberRange deserializeNumberRange(@Nullable JsonElement element, @Nullable String key){
        return NumberRange.deserialize(this, element, key);
    }

    /**
     * Utility method for serializing number ranges. This exists because the default NumberRange serialization method,
     * {@link NumberRange#serialize(LootTableLoader)}, does not fit for the standard {@code Function<T, JsonElement>}
     * that some methods in this library use.
     */
    public @NotNull JsonObject serializeNumberRange(@NotNull NumberRange range){
        return range.serialize(this);
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
        public @NotNull Builder numberProviderBuilder(@NotNull Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder){
            this.numberProviderBuilder = numberProviderBuilder;
            return this;
        }

        /**
         * Sets the builder that is used for creating the {@link #getLootConditionManager()} ()}
         */
        @Contract("_ -> this")
        public @NotNull Builder lootConditionBuilder(@NotNull Consumer<JsonSerializationManager.Builder<LootCondition>> lootConditionBuilder){
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
