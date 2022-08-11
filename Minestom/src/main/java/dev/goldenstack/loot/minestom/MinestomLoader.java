package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.converter.meta.LootConversionManager;
import dev.goldenstack.loot.minestom.entry.*;
import dev.goldenstack.loot.minestom.generation.StandardLootPool;
import dev.goldenstack.loot.minestom.generation.StandardLootTable;
import dev.goldenstack.loot.minestom.number.BinomialNumber;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.minestom.number.UniformNumber;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Utilities and functions to initialize ImmuTables instances for Minestom.
 */
public class MinestomLoader {
    private MinestomLoader() {}

    /**
     * The general initializer for ImmuTables builders. If this is applied to a builder, none of the other consumers in
     * this class will need to be applied to it, and neither will {@link StandardLootPool#CONVERTER} and
     * {@link StandardLootTable#CONVERTER}.
     */
    public static final @NotNull Consumer<ImmuTables.Builder<ItemStack>> GENERAL_BUILDER_INITIALIZER = builder -> {
        builder
                .lootEntryBuilder(MinestomLoader.LOOT_ENTRY_INITIALIZER)
                .lootModifierBuilder(MinestomLoader.LOOT_MODIFIER_INITIALIZER)
                .lootConditionBuilder(MinestomLoader.LOOT_CONDITION_INITIALIZER)
                .lootNumberBuilder(MinestomLoader.LOOT_NUMBER_INITIALIZER)
                .lootPoolConverter(StandardLootPool.CONVERTER)
                .lootTableConverter(StandardLootTable.CONVERTER);
    };

    /**
     * When passed into {@link dev.goldenstack.loot.ImmuTables.Builder#lootEntryBuilder(Consumer)}, adds the required
     * information to the loader.
     */
    public static final @NotNull Consumer<LootConversionManager.Builder<ItemStack, LootEntry<ItemStack>>> LOOT_ENTRY_INITIALIZER = builder -> {
        // Basic data
        builder.baseType(new TypeToken<>(){}); // LootEntry<ItemStack>
        builder.keyLocation("type");

        // Registered converters
        builder.addConverter(AlternativeEntry.CONVERTER);
        builder.addConverter(EmptyEntry.CONVERTER);
        builder.addConverter(GroupEntry.CONVERTER);
        builder.addConverter(ItemEntry.CONVERTER);
        builder.addConverter(SequenceEntry.CONVERTER);
    };

    /**
     * When passed into {@link dev.goldenstack.loot.ImmuTables.Builder#lootModifierBuilder(Consumer)}, adds the required
     * information to the loader.
     */
    public static final @NotNull Consumer<LootConversionManager.Builder<ItemStack, LootModifier<ItemStack>>> LOOT_MODIFIER_INITIALIZER = builder -> {
        // Basic data
        builder.baseType(new TypeToken<>(){}); // LootModifier<ItemStack>
        builder.keyLocation("function");

        // Registered converters
        // (none currently)
    };

    /**
     * When passed into {@link dev.goldenstack.loot.ImmuTables.Builder#lootConditionBuilder(Consumer)}, adds the required
     * information to the loader.
     */
    public static final @NotNull Consumer<LootConversionManager.Builder<ItemStack, LootCondition<ItemStack>>> LOOT_CONDITION_INITIALIZER = builder -> {
        // Basic data
        builder.baseType(new TypeToken<>(){}); // LootCondition<ItemStack>
        builder.keyLocation("condition");

        // Registered converters
        // (none currently)
    };

    /**
     * When passed into {@link dev.goldenstack.loot.ImmuTables.Builder#lootNumberBuilder(Consumer)}, adds the required
     * information to the loader.
     */
    public static final @NotNull Consumer<LootConversionManager.Builder<ItemStack, LootNumber<ItemStack>>> LOOT_NUMBER_INITIALIZER = builder -> {
        // Basic data
        builder.baseType(new TypeToken<>(){}); // LootNumber<ItemStack>
        builder.keyLocation("type");

        // Registered converters
        builder.addInitialConverter(ConstantNumber.ACCURATE_CONVERTER);
        builder.addConverter(ConstantNumber.CONVERTER);
        builder.addConverter(BinomialNumber.CONVERTER);
        builder.addConverter(UniformNumber.CONVERTER);
    };

}
