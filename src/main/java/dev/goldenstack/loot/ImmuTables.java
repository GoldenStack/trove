package dev.goldenstack.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.condition.*;
import dev.goldenstack.loot.context.LootParameterGroup;
import dev.goldenstack.loot.entry.*;
import dev.goldenstack.loot.function.*;
import dev.goldenstack.loot.json.JsonSerializationManager;
import dev.goldenstack.loot.provider.number.BinomiallyDistributedNumber;
import dev.goldenstack.loot.provider.number.ConstantNumber;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.provider.number.UniformNumber;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class for serializing and deserializing loot tables
 */
public class ImmuTables {

    private final @NotNull JsonSerializationManager<NumberProvider> numberProviderManager;
    private final @NotNull JsonSerializationManager<LootCondition> lootConditionManager;
    private final @NotNull JsonSerializationManager<LootFunction> lootFunctionManager;
    private final @NotNull JsonSerializationManager<LootEntry> lootEntryManager;

    private final @NotNull Map<NamespaceID, LootParameterGroup> lootParameterGroupRegistry;

    private ImmuTables(@NotNull Builder builder) {
        this.numberProviderManager = new JsonSerializationManager<>(this, builder.numberProviderElementName);
        this.lootConditionManager = new JsonSerializationManager<>(this, builder.lootConditionElementName);
        this.lootFunctionManager = new JsonSerializationManager<>(this, builder.lootFunctionElementName);
        this.lootEntryManager = new JsonSerializationManager<>(this, builder.lootEntryElementName);

        this.lootParameterGroupRegistry = new ConcurrentHashMap<>();
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
     * Returns the map that controls how loot parameter groups are registered.
     */
    public @NotNull Map<NamespaceID, LootParameterGroup> getLootParameterGroupRegistry() {
        return lootParameterGroupRegistry;
    }

    /**
     * Utility method for deserializing number ranges. This exists because the default NumberRange deserialization
     * method, {@link NumberRange#deserialize(ImmuTables, JsonElement, String)}, does not fit for the standard
     * {@code BiFunction<JsonElement, String, T>} that some methods in this library use.
     */
    public @NotNull NumberRange deserializeNumberRange(@Nullable JsonElement element, @Nullable String key) {
        return NumberRange.deserialize(this, element, key);
    }

    /**
     * Utility method for serializing number ranges. This exists because the default NumberRange serialization method,
     * {@link NumberRange#serialize(ImmuTables)}, does not fit for the standard {@code Function<T, JsonElement>}
     * that some methods in this library use.
     */
    public @NotNull JsonObject serializeNumberRange(@NotNull NumberRange range) {
        return range.serialize(this);
    }

    /**
     * Creates a new {@link Builder}
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Utility class for building {@code ImmuTables} instances
     */
    public static class Builder {

        /**
         * Adds the default values for the NumberProvider manager to the provided builder
         */
        public static void setupNumberProviderManager(@NotNull JsonSerializationManager<NumberProvider> manager) {
            manager.defaultDeserializer(ConstantNumber.DEFAULT_DESERIALIZER);
            manager.register(ConstantNumber.CONVERTER);
            manager.register(UniformNumber.CONVERTER);
            manager.register(BinomiallyDistributedNumber.CONVERTER);
        }

        /**
         * Adds the default values for the LootCondition manager to the provided builder
         */
        public static void setupLootConditionManager(@NotNull JsonSerializationManager<LootCondition> manager) {
            manager.register(AlternativeCondition.CONVERTER);
            manager.register(BlockStatePropertyCondition.CONVERTER);
            manager.register(InvertedCondition.CONVERTER);
            manager.register(KilledByPlayerCondition.CONVERTER);
            manager.register(LootingRandomChanceCondition.CONVERTER);
            manager.register(RandomChanceCondition.CONVERTER);
            manager.register(SurvivesExplosionCondition.CONVERTER);
            manager.register(TimeCheckCondition.CONVERTER);
            manager.register(ValueCheckCondition.CONVERTER);
        }

        /**
         * Adds the default values for the LootFunction manager to the provided builder
         */
        public static void setupLootFunctionManager(@NotNull JsonSerializationManager<LootFunction> manager) {
            manager.register(AddAttributesFunction.CONVERTER);
            manager.register(AddDamageFunction.CONVERTER);
            manager.register(ExplosionDecayFunction.CONVERTER);
            manager.register(LimitCountFunction.CONVERTER);
            manager.register(SetCountFunction.CONVERTER);
            manager.register(SetEnchantmentsFunction.CONVERTER);
        }

        /**
         * Adds the default values for the LootEntry manager to the provided builder
         */
        public static void setupLootEntryManager(@NotNull JsonSerializationManager<LootEntry> manager) {
            manager.register(AlternativeEntry.CONVERTER);
            manager.register(EmptyEntry.CONVERTER);
            manager.register(GroupEntry.CONVERTER);
            manager.register(ItemEntry.CONVERTER);
            manager.register(SequenceEntry.CONVERTER);
        }

        private Builder() {}

        private String numberProviderElementName, lootConditionElementName, lootFunctionElementName, lootEntryElementName;

        @Contract(" -> this")
        public @NotNull Builder setDefaultValues() {
            this.numberProviderElementName = "type";
            this.lootConditionElementName = "condition";
            this.lootFunctionElementName = "function";
            this.lootEntryElementName = "type";
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder numberProviderElementName(@NotNull String numberProviderElementName) {
            this.numberProviderElementName = numberProviderElementName;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootConditionElementName(@NotNull String lootConditionElementName) {
            this.lootConditionElementName = lootConditionElementName;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootEntryElementName(@NotNull String lootEntryElementName) {
            this.lootEntryElementName = lootEntryElementName;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder lootFunctionElementName(@NotNull String lootFunctionElementName) {
            this.lootFunctionElementName = lootFunctionElementName;
            return this;
        }

        /**
         * Builds a {@code ImmuTables} instance from this builder.
         */
        public @NotNull ImmuTables build() {
            Objects.requireNonNull(this.numberProviderElementName, "Number provider element name must not be null!");
            Objects.requireNonNull(this.lootConditionElementName, "Loot condition element name must not be null!");
            Objects.requireNonNull(this.lootFunctionElementName, "Loot function element name must not be null!");
            Objects.requireNonNull(this.lootEntryElementName, "Loot entry element name must not be null!");
            return new ImmuTables(this);
        }

    }
}
