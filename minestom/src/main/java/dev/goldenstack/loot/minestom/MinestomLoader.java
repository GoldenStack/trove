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
        builder.addConverter(AlternativeEntry.KEY, AlternativeEntry.CONVERTER);
        builder.addConverter(DynamicEntry.KEY, DynamicEntry.CONVERTER);
        builder.addConverter(EmptyEntry.KEY, EmptyEntry.CONVERTER);
        builder.addConverter(GroupEntry.KEY, GroupEntry.CONVERTER);
        builder.addConverter(ItemEntry.KEY, ItemEntry.CONVERTER);
        builder.addConverter(SequenceEntry.KEY, SequenceEntry.CONVERTER);
        builder.addConverter(TableEntry.KEY, TableEntry.CONVERTER);
        builder.addConverter(TagEntry.KEY, TagEntry.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootModifier> createModifierBuilder() {
        LootConversionManager.Builder<LootModifier> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootModifier.class));
        builder.keyLocation("function");

        // Registered converters
        builder.addConverter(ApplyLootingModifier.KEY, ApplyLootingModifier.CONVERTER);
        builder.addConverter(BonusCountModifier.KEY, BonusCountModifier.CONVERTER);
        builder.addConverter(CopyNameModifier.KEY, CopyNameModifier.CONVERTER);
        builder.addConverter(CopyNbtModifier.KEY, CopyNbtModifier.CONVERTER);
        builder.addConverter(CopyStateModifier.KEY, CopyStateModifier.CONVERTER);
        builder.addConverter(ExplosionDecayModifier.KEY, ExplosionDecayModifier.CONVERTER);
        builder.addConverter(LevelledEnchantModifier.KEY, LevelledEnchantModifier.CONVERTER);
        builder.addConverter(LimitCountModifier.KEY, LimitCountModifier.CONVERTER);
        builder.addConverter(RandomlyEnchantModifier.KEY, RandomlyEnchantModifier.CONVERTER);
        builder.addConverter(SetAttributesModifier.KEY, SetAttributesModifier.CONVERTER);
        builder.addConverter(SetContentsModifier.KEY, SetContentsModifier.CONVERTER);
        builder.addConverter(SetCountModifier.KEY, SetCountModifier.CONVERTER);
        builder.addConverter(SetDamageModifier.KEY, SetDamageModifier.CONVERTER);
        builder.addConverter(SetNbtModifier.KEY, SetNbtModifier.CONVERTER);
        builder.addConverter(SetPotionModifier.KEY, SetPotionModifier.CONVERTER);
        builder.addConverter(SetStewEffectModifier.KEY, SetStewEffectModifier.CONVERTER);
        builder.addConverter(SmeltItemModifier.KEY, SmeltItemModifier.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootCondition> createConditionBuilder() {
        LootConversionManager.Builder<LootCondition> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootCondition.class));
        builder.keyLocation("condition");

        // Registered converters
        builder.addConverter(AndCondition.KEY, AndCondition.CONVERTER);
        builder.addConverter(BlockStateCondition.KEY, BlockStateCondition.CONVERTER);
        builder.addConverter(EnchantmentLevelCondition.KEY, EnchantmentLevelCondition.CONVERTER);
        builder.addConverter(EntityCheckCondition.KEY, EntityCheckCondition.CONVERTER);
        builder.addConverter(InvertedCondition.KEY, InvertedCondition.CONVERTER);
        builder.addConverter(KilledByPlayerCondition.KEY, KilledByPlayerCondition.CONVERTER);
        builder.addConverter(LocationCheckCondition.KEY, LocationCheckCondition.CONVERTER);
        builder.addConverter(LootingRandomChanceCondition.KEY, LootingRandomChanceCondition.CONVERTER);
        builder.addConverter(NumberConstraintCondition.KEY, NumberConstraintCondition.CONVERTER);
        builder.addConverter(OrCondition.KEY, OrCondition.CONVERTER);
        builder.addConverter(RandomChanceCondition.KEY, RandomChanceCondition.CONVERTER);
        builder.addConverter(ReferenceCondition.KEY, ReferenceCondition.CONVERTER);
        builder.addConverter(SurvivesExplosionCondition.KEY, SurvivesExplosionCondition.CONVERTER);
        builder.addConverter(TimeCheckCondition.KEY, TimeCheckCondition.CONVERTER);
        builder.addConverter(ToolCheckCondition.KEY, ToolCheckCondition.CONVERTER);
        builder.addConverter(WeatherCheckCondition.KEY, WeatherCheckCondition.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootNumber> createNumberBuilder() {
        LootConversionManager.Builder<LootNumber> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootNumber.class));
        builder.keyLocation("type");

        // Registered converters
        builder.addInitialConverter(ConstantNumber.ACCURATE_CONVERTER);
        builder.addConverter(ConstantNumber.KEY, ConstantNumber.CONVERTER);
        builder.addConverter(BinomialNumber.KEY, BinomialNumber.CONVERTER);
        builder.addConverter(UniformNumber.KEY, UniformNumber.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootNBT> createNbtBuilder() {
        LootConversionManager.Builder<LootNBT> builder = LootConversionManager.builder();

        // Basic data
        builder.baseType(TypeToken.get(LootNBT.class));
        builder.keyLocation("type");

        // Registered converters
        builder.addInitialConverter(ContextNBT.ACCURATE_CONVERTER);
        builder.addConverter(ContextNBT.KEY, ContextNBT.CONVERTER);
        builder.addConverter(StorageNBT.KEY, StorageNBT.CONVERTER);

        return builder;
    }

}
