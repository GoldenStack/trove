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
        LootConversionManager.Builder<LootEntry> builder = LootConversionManager.builder(TypeToken.get(LootEntry.class));

        // Basic data
        builder.keyLocation("type");

        // Registered converters
        builder.add(AlternativeEntry.KEY, AlternativeEntry.CONVERTER);
        builder.add(DynamicEntry.KEY, DynamicEntry.CONVERTER);
        builder.add(EmptyEntry.KEY, EmptyEntry.CONVERTER);
        builder.add(GroupEntry.KEY, GroupEntry.CONVERTER);
        builder.add(ItemEntry.KEY, ItemEntry.CONVERTER);
        builder.add(SequenceEntry.KEY, SequenceEntry.CONVERTER);
        builder.add(TableEntry.KEY, TableEntry.CONVERTER);
        builder.add(TagEntry.KEY, TagEntry.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootModifier> createModifierBuilder() {
        LootConversionManager.Builder<LootModifier> builder = LootConversionManager.builder(TypeToken.get(LootModifier.class));

        // Basic data
        builder.keyLocation("function");

        // Registered converters
        builder.add(ApplyLootingModifier.KEY, ApplyLootingModifier.CONVERTER);
        builder.add(BonusCountModifier.KEY, BonusCountModifier.CONVERTER);
        builder.add(CopyNameModifier.KEY, CopyNameModifier.CONVERTER);
        builder.add(CopyNbtModifier.KEY, CopyNbtModifier.CONVERTER);
        builder.add(CopyStateModifier.KEY, CopyStateModifier.CONVERTER);
        builder.add(ExplosionDecayModifier.KEY, ExplosionDecayModifier.CONVERTER);
        builder.add(LevelledEnchantModifier.KEY, LevelledEnchantModifier.CONVERTER);
        builder.add(LimitCountModifier.KEY, LimitCountModifier.CONVERTER);
        builder.add(RandomlyEnchantModifier.KEY, RandomlyEnchantModifier.CONVERTER);
        builder.add(SetAttributesModifier.KEY, SetAttributesModifier.CONVERTER);
        builder.add(SetContentsModifier.KEY, SetContentsModifier.CONVERTER);
        builder.add(SetCountModifier.KEY, SetCountModifier.CONVERTER);
        builder.add(SetDamageModifier.KEY, SetDamageModifier.CONVERTER);
        builder.add(SetNbtModifier.KEY, SetNbtModifier.CONVERTER);
        builder.add(SetPotionModifier.KEY, SetPotionModifier.CONVERTER);
        builder.add(SetStewEffectModifier.KEY, SetStewEffectModifier.CONVERTER);
        builder.add(SmeltItemModifier.KEY, SmeltItemModifier.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootCondition> createConditionBuilder() {
        LootConversionManager.Builder<LootCondition> builder = LootConversionManager.builder(TypeToken.get(LootCondition.class));

        // Basic data
        builder.keyLocation("condition");

        // Registered converters
        builder.add(AndCondition.KEY, AndCondition.CONVERTER);
        builder.add(BlockStateCondition.KEY, BlockStateCondition.CONVERTER);
        builder.add(EnchantmentLevelCondition.KEY, EnchantmentLevelCondition.CONVERTER);
        builder.add(EntityCheckCondition.KEY, EntityCheckCondition.CONVERTER);
        builder.add(InvertedCondition.KEY, InvertedCondition.CONVERTER);
        builder.add(KilledByPlayerCondition.KEY, KilledByPlayerCondition.CONVERTER);
        builder.add(LocationCheckCondition.KEY, LocationCheckCondition.CONVERTER);
        builder.add(LootingRandomChanceCondition.KEY, LootingRandomChanceCondition.CONVERTER);
        builder.add(NumberConstraintCondition.KEY, NumberConstraintCondition.CONVERTER);
        builder.add(OrCondition.KEY, OrCondition.CONVERTER);
        builder.add(RandomChanceCondition.KEY, RandomChanceCondition.CONVERTER);
        builder.add(ReferenceCondition.KEY, ReferenceCondition.CONVERTER);
        builder.add(SurvivesExplosionCondition.KEY, SurvivesExplosionCondition.CONVERTER);
        builder.add(TimeCheckCondition.KEY, TimeCheckCondition.CONVERTER);
        builder.add(ToolCheckCondition.KEY, ToolCheckCondition.CONVERTER);
        builder.add(WeatherCheckCondition.KEY, WeatherCheckCondition.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootNumber> createNumberBuilder() {
        LootConversionManager.Builder<LootNumber> builder = LootConversionManager.builder(TypeToken.get(LootNumber.class));

        // Basic data
        builder.keyLocation("type");

        // Registered converters
        builder.add(ConstantNumber.ACCURATE_CONVERTER);
        builder.add(ConstantNumber.KEY, ConstantNumber.CONVERTER);
        builder.add(BinomialNumber.KEY, BinomialNumber.CONVERTER);
        builder.add(UniformNumber.KEY, UniformNumber.CONVERTER);

        return builder;
    }

    public static @NotNull LootConversionManager.Builder<LootNBT> createNbtBuilder() {
        LootConversionManager.Builder<LootNBT> builder = LootConversionManager.builder(TypeToken.get(LootNBT.class));

        // Basic data
        builder.keyLocation("type");

        // Registered converters
        builder.add(ContextNBT.ACCURATE_CONVERTER);
        builder.add(ContextNBT.KEY, ContextNBT.CONVERTER);
        builder.add(StorageNBT.KEY, StorageNBT.CONVERTER);

        return builder;
    }

}
