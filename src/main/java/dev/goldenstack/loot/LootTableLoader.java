package dev.goldenstack.loot;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.enchantment.EnchantmentManager;
import dev.goldenstack.loot.condition.*;
import dev.goldenstack.loot.context.LootParameterGroup;
import dev.goldenstack.loot.entry.LootEntry;
import dev.goldenstack.loot.function.*;
import dev.goldenstack.loot.json.JsonSerializationManager;
import dev.goldenstack.loot.provider.number.BinomiallyDistributedNumber;
import dev.goldenstack.loot.provider.number.ConstantNumber;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.provider.number.UniformNumber;
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
    private final @NotNull JsonSerializationManager<LootFunction> lootFunctionManager;
    private final @NotNull JsonSerializationManager<LootEntry> lootEntryManager;

    private final @NotNull BiMap<String, LootParameterGroup> lootParameterGroupRegistry;

    private final @NotNull EnchantmentManager enchantmentManager;

    private LootTableLoader(@NotNull Builder builder){

        // Number provider manager
        JsonSerializationManager.Builder<NumberProvider> numberProviderBuilder = JsonSerializationManager.builder();
        if (builder.numberProviderBuilder != null){
            builder.numberProviderBuilder.accept(numberProviderBuilder);
        }
        this.numberProviderManager = numberProviderBuilder.owner(this).build();

        // Loot condition manager
        JsonSerializationManager.Builder<LootCondition> lootConditionBuilder = JsonSerializationManager.builder();
        if (builder.lootConditionBuilder != null){
            builder.lootConditionBuilder.accept(lootConditionBuilder);
        }
        this.lootConditionManager = lootConditionBuilder.owner(this).build();

        // Loot function manager
        JsonSerializationManager.Builder<LootFunction> lootFunctionBuilder = JsonSerializationManager.builder();
        if (builder.lootFunctionBuilder != null){
            builder.lootFunctionBuilder.accept(lootFunctionBuilder);
        }
        this.lootFunctionManager = lootFunctionBuilder.owner(this).build();

        // Loot entry manager
        JsonSerializationManager.Builder<LootEntry> lootEntryBuilder = JsonSerializationManager.builder();
        if (builder.lootEntryBuilder != null){
            builder.lootEntryBuilder.accept(lootEntryBuilder);
        }
        this.lootEntryManager = lootEntryBuilder.owner(this).build();

        // Loot parameter group registry
        lootParameterGroupRegistry = HashBiMap.create();

        // Enchantment manager
        EnchantmentManager.Builder enchantmentBuilder = EnchantmentManager.builder();
        if (builder.enchantmentManagerBuilder != null){
            builder.enchantmentManagerBuilder.accept(enchantmentBuilder);
        }
        this.enchantmentManager = enchantmentBuilder.build();

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
     * Returns the JsonSerializationManager that is used to serialize/deserialize {@code LootFunction}s
     */
    public @NotNull JsonSerializationManager<LootFunction> getLootFunctionManager() {
        return lootFunctionManager;
    }

    /**
     * Returns the JsonSerializationManager that is used to serialize/deserialize {@code LootEntry} instances.
     */
    public @NotNull JsonSerializationManager<LootEntry> getLootEntryManager() {
        return lootEntryManager;
    }

    /**
     * Returns the BiMap that controls how loot parameter groups are registered.
     */
    public @NotNull BiMap<String, LootParameterGroup> getLootParameterGroupRegistry(){
        return lootParameterGroupRegistry;
    }

    /**
     * Returns the EnchantmentManager that is used for some loot functions.
     */
    public @NotNull EnchantmentManager getEnchantmentManager(){
        return enchantmentManager;
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

        /**
         * Adds the default values for the NumberProvider manager to the provided builder
         */
        public static void setupNumberProviderBuilder(@NotNull JsonSerializationManager.Builder<NumberProvider> builder){
            builder.elementName("type")
                   .defaultDeserializer(ConstantNumber::defaultDeserializer)
                   .putDeserializer(ConstantNumber.KEY, ConstantNumber::deserialize)
                   .putDeserializer(UniformNumber.KEY, UniformNumber::deserialize)
                   .putDeserializer(BinomiallyDistributedNumber.KEY, BinomiallyDistributedNumber::deserialize);
        }

        /**
         * Adds the default values for the LootCondition manager to the provided builder
         */
        public static void setupLootConditionManager(@NotNull JsonSerializationManager.Builder<LootCondition> builder){
            builder.elementName("condition")
                   .putDeserializer(RandomChanceCondition.KEY, RandomChanceCondition::deserialize)
                   .putDeserializer(LootingRandomChanceCondition.KEY, LootingRandomChanceCondition::deserialize)
                   .putDeserializer(KilledByPlayerCondition.KEY, KilledByPlayerCondition::deserialize)
                   .putDeserializer(InvertedCondition.KEY, InvertedCondition::deserialize)
                   .putDeserializer(BlockStatePropertyCondition.KEY, BlockStatePropertyCondition::deserialize)
                   .putDeserializer(SurvivesExplosionCondition.KEY, SurvivesExplosionCondition::deserialize)
                   .putDeserializer(TimeCheckCondition.KEY, TimeCheckCondition::deserialize)
                   .putDeserializer(AlternativeCondition.KEY, AlternativeCondition::deserialize)
                   .putDeserializer(ValueCheckCondition.KEY, ValueCheckCondition::deserialize);
        }

        /**
         * Adds the default values for the LootFunction manager to the provided builder
         */
        public static void setupLootFunctionManager(@NotNull JsonSerializationManager.Builder<LootFunction> builder){
            builder.elementName("function")
                   .putDeserializer(SetCountFunction.KEY, SetCountFunction::deserialize)
                   .putDeserializer(LimitCountFunction.KEY, LimitCountFunction::deserialize)
                   .putDeserializer(AddDamageFunction.KEY, AddDamageFunction::deserialize)
                   .putDeserializer(AddAttributesFunction.KEY, AddAttributesFunction::deserialize)
                   .putDeserializer(ExplosionDecayFunction.KEY, ExplosionDecayFunction::deserialize)
                   .putDeserializer(SetEnchantmentsFunction.KEY, SetEnchantmentsFunction::deserialize);
        }

        /**
         * Adds the default values for the LootEntry manager to the provided builder
         */
        public static void setupLootEntryManager(@NotNull JsonSerializationManager.Builder<LootEntry> builder){
            builder.elementName("type");
        }

        /**
         * Adds the default values for the EnchantmentManager to the provided builder
         */
        public static void setupEnchantmentManagerBuilder(@NotNull EnchantmentManager.Builder builder){
            builder.useConcurrentHashMap(false)
                   .useDefaultEnchantability(true)
                   .useDefaultEnchantmentData(true);
        }

        private Builder(){}

        private Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder;
        private Consumer<JsonSerializationManager.Builder<LootCondition>> lootConditionBuilder;
        private Consumer<JsonSerializationManager.Builder<LootFunction>> lootFunctionBuilder;
        private Consumer<JsonSerializationManager.Builder<LootEntry>> lootEntryBuilder;

        private Consumer<EnchantmentManager.Builder> enchantmentManagerBuilder;

        /**
         * Sets the builder that is used for creating the {@link #getNumberProviderManager()}
         */
        @Contract("_ -> this")
        public @NotNull Builder numberProviderBuilder(@NotNull Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder){
            this.numberProviderBuilder = numberProviderBuilder;
            return this;
        }

        /**
         * Sets the builder that is used for creating the {@link #getLootConditionManager()}
         */
        @Contract("_ -> this")
        public @NotNull Builder lootConditionBuilder(@NotNull Consumer<JsonSerializationManager.Builder<LootCondition>> lootConditionBuilder){
            this.lootConditionBuilder = lootConditionBuilder;
            return this;
        }

        /**
         * Sets the builder that is used for creating the {@link #getLootFunctionManager()}
         */
        @Contract("_ -> this")
        public @NotNull Builder lootFunctionBuilder(@NotNull Consumer<JsonSerializationManager.Builder<LootFunction>> lootFunctionBuilder){
            this.lootFunctionBuilder = lootFunctionBuilder;
            return this;
        }

        /**
         * Sets the builder that is used for creating the {@link #getLootEntryManager()}
         */
        @Contract("_ -> this")
        public @NotNull Builder lootEntryBuilder(@NotNull Consumer<JsonSerializationManager.Builder<LootEntry>> lootEntryBuilder){
            this.lootEntryBuilder = lootEntryBuilder;
            return this;
        }

        /**
         * Sets the builder that is used for creating the {@link #getEnchantmentManager()}
         */
        @Contract("_ -> this")
        public @NotNull Builder enchantmentManagerBuilder(@NotNull Consumer<EnchantmentManager.Builder> enchantmentManagerBuilder){
            this.enchantmentManagerBuilder = enchantmentManagerBuilder;
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
