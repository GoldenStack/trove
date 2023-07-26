package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.Trove;
import dev.goldenstack.loot.converter.meta.LootConversionManager;
import dev.goldenstack.loot.minestom.condition.*;
import dev.goldenstack.loot.minestom.entry.*;
import dev.goldenstack.loot.minestom.generation.LootPool;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.modifier.*;
import dev.goldenstack.loot.minestom.nbt.ContextNBT;
import dev.goldenstack.loot.minestom.nbt.LootNBT;
import dev.goldenstack.loot.minestom.nbt.StorageNBT;
import dev.goldenstack.loot.minestom.number.BinomialNumber;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.minestom.number.UniformNumber;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities and functions to initialize Trove instances for Minestom.
 */
public class MinestomLoader {
    private MinestomLoader() {}

    /**
     * The general initializer for Trove builders. If this is applied to a builder, none of the other consumers in
     * this class will need to be applied to it, and neither will {@link LootPool#CONVERTER} and
     * {@link LootTable#CONVERTER}.
     */
    public static @NotNull Trove.Builder initializeBuilder(@NotNull Trove.Builder builder) {
        return builder
                .add(createEntryBuilder().build())
                .add(createModifierBuilder().build())
                .add(createConditionBuilder().build())
                .add(createNumberBuilder().build())
                .add(createNbtBuilder().build());
    }

    public static @NotNull LootConversionManager.Builder<LootEntry> createEntryBuilder() {
        LootConversionManager.Builder<LootEntry> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootEntry.class));
        builder.keyLocation("type");

        // Registered converters
        builder.addConverter(AlternativeEntry.CONVERTER);
        builder.addConverter(DynamicEntry.CONVERTER);
        builder.addConverter(EmptyEntry.CONVERTER);
        builder.addConverter(GroupEntry.CONVERTER);
        builder.addConverter(ItemEntry.CONVERTER);
        builder.addConverter(SequenceEntry.CONVERTER);
        builder.addConverter(TableEntry.CONVERTER);
        builder.addConverter(TagEntry.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootModifier> createModifierBuilder() {
        LootConversionManager.Builder<LootModifier> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootModifier.class));
        builder.keyLocation("function");

        // Registered converters
        builder.addConverter(ApplyLootingModifier.CONVERTER);
        builder.addConverter(BonusCountModifier.CONVERTER);
        builder.addConverter(CopyNameModifier.CONVERTER);
        builder.addConverter(CopyNbtModifier.CONVERTER);
        builder.addConverter(CopyStateModifier.CONVERTER);
        builder.addConverter(ExplosionDecayModifier.CONVERTER);
        builder.addConverter(LevelledEnchantModifier.CONVERTER);
        builder.addConverter(LimitCountModifier.CONVERTER);
        builder.addConverter(RandomlyEnchantModifier.CONVERTER);
        builder.addConverter(SetAttributesModifier.CONVERTER);
        builder.addConverter(SetContentsModifier.CONVERTER);
        builder.addConverter(SetCountModifier.CONVERTER);
        builder.addConverter(SetDamageModifier.CONVERTER);
        builder.addConverter(SetNbtModifier.CONVERTER);
        builder.addConverter(SetPotionModifier.CONVERTER);
        builder.addConverter(SetStewEffectModifier.CONVERTER);
        builder.addConverter(SmeltItemModifier.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootCondition> createConditionBuilder() {
        LootConversionManager.Builder<LootCondition> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootCondition.class));
        builder.keyLocation("condition");

        // Registered converters
        builder.addConverter(AndCondition.CONVERTER);
        builder.addConverter(BlockStateCondition.CONVERTER);
        builder.addConverter(EnchantmentLevelCondition.CONVERTER);
        builder.addConverter(EntityCheckCondition.CONVERTER);
        builder.addConverter(InvertedCondition.CONVERTER);
        builder.addConverter(KilledByPlayerCondition.CONVERTER);
        builder.addConverter(LocationCheckCondition.CONVERTER);
        builder.addConverter(LootingRandomChanceCondition.CONVERTER);
        builder.addConverter(NumberConstraintCondition.CONVERTER);
        builder.addConverter(OrCondition.CONVERTER);
        builder.addConverter(RandomChanceCondition.CONVERTER);
        builder.addConverter(ReferenceCondition.CONVERTER);
        builder.addConverter(SurvivesExplosionCondition.CONVERTER);
        builder.addConverter(TimeCheckCondition.CONVERTER);
        builder.addConverter(ToolCheckCondition.CONVERTER);
        builder.addConverter(WeatherCheckCondition.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootNumber> createNumberBuilder() {
        LootConversionManager.Builder<LootNumber> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootNumber.class));
        builder.keyLocation("type");

        // Registered converters
        builder.addInitialConverter(ConstantNumber.ACCURATE_CONVERTER);
        builder.addConverter(ConstantNumber.CONVERTER);
        builder.addConverter(BinomialNumber.CONVERTER);
        builder.addConverter(UniformNumber.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootNBT> createNbtBuilder() {
        LootConversionManager.Builder<LootNBT> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootNBT.class));
        builder.keyLocation("type");

        // Registered converters
        builder.addInitialConverter(ContextNBT.ACCURATE_CONVERTER);
        builder.addConverter(ContextNBT.CONVERTER);
        builder.addConverter(StorageNBT.CONVERTER);

        return builder;
    }

}
