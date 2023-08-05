package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.converter.generator.FieldTypes;
import dev.goldenstack.loot.converter.generator.LootConversionManager;
import dev.goldenstack.loot.minestom.condition.*;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.minestom.entry.*;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.modifier.*;
import dev.goldenstack.loot.minestom.nbt.ContextNBT;
import dev.goldenstack.loot.minestom.nbt.LootNBT;
import dev.goldenstack.loot.minestom.nbt.StorageNBT;
import dev.goldenstack.loot.minestom.number.BinomialNumber;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.minestom.number.UniformNumber;
import dev.goldenstack.loot.minestom.util.FallbackVanillaInterface;
import dev.goldenstack.loot.minestom.util.MinestomTypes;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.goldenstack.loot.minestom.context.LootContextKeyGroup.*;

/**
 * Provides utilities for easily reading loot tables.
 */
public class TroveMinestom {

    /**
     * The standard map of key groups.
     */
    public static final @NotNull Map<String, LootContextKeyGroup> STANDARD_GROUPS = Stream.of(
            EMPTY, CHEST, COMMAND, SELECTOR, FISHING, ENTITY, ARCHAEOLOGY, GIFT, BARTER, ADVANCEMENT_REWARD, ADVANCEMENT_ENTITY, ADVANCEMENT_LOCATION, GENERIC, BLOCK
    ).collect(Collectors.toMap(LootContextKeyGroup::id, Function.identity()));


    public static final @NotNull TypeSerializerCollection INSTANCE_COLLECTION = TypeSerializerCollection.builder()
            .register(AlternativeEntry.class, AlternativeEntry.CONVERTER)
            .register(DynamicEntry.class, DynamicEntry.CONVERTER)
            .register(EmptyEntry.class, EmptyEntry.CONVERTER)
            .register(GroupEntry.class, GroupEntry.CONVERTER)
            .register(ItemEntry.class, ItemEntry.CONVERTER)
            .register(SequenceEntry.class, SequenceEntry.CONVERTER)
            .register(TableEntry.class, TableEntry.CONVERTER)
            .register(TagEntry.class, TagEntry.CONVERTER)
            .register(ApplyLootingModifier.class, ApplyLootingModifier.CONVERTER)
            .register(BonusCountModifier.class, BonusCountModifier.CONVERTER)
            .register(CopyNameModifier.class, CopyNameModifier.CONVERTER)
            .register(CopyNbtModifier.class, CopyNbtModifier.CONVERTER)
            .register(CopyStateModifier.class, CopyStateModifier.CONVERTER)
            .register(ExplosionDecayModifier.class, ExplosionDecayModifier.CONVERTER)
            .register(LevelledEnchantModifier.class, LevelledEnchantModifier.CONVERTER)
            .register(LimitCountModifier.class, LimitCountModifier.CONVERTER)
            .register(RandomlyEnchantModifier.class, RandomlyEnchantModifier.CONVERTER)
            .register(SetAttributesModifier.class, SetAttributesModifier.CONVERTER)
            .register(SetContentsModifier.class, SetContentsModifier.CONVERTER)
            .register(SetCountModifier.class, SetCountModifier.CONVERTER)
            .register(SetDamageModifier.class, SetDamageModifier.CONVERTER)
            .register(SetNbtModifier.class, SetNbtModifier.CONVERTER)
            .register(SetPotionModifier.class, SetPotionModifier.CONVERTER)
            .register(SetStewEffectModifier.class, SetStewEffectModifier.CONVERTER)
            .register(SmeltItemModifier.class, SmeltItemModifier.CONVERTER)
            .register(AndCondition.class, AndCondition.CONVERTER)
            .register(BlockStateCondition.class, BlockStateCondition.CONVERTER)
            .register(EnchantmentLevelCondition.class, EnchantmentLevelCondition.CONVERTER)
            .register(EntityCheckCondition.class, EntityCheckCondition.CONVERTER)
            .register(InvertedCondition.class, InvertedCondition.CONVERTER)
            .register(KilledByPlayerCondition.class, KilledByPlayerCondition.CONVERTER)
            .register(LocationCheckCondition.class, LocationCheckCondition.CONVERTER)
            .register(LootingRandomChanceCondition.class, LootingRandomChanceCondition.CONVERTER)
            .register(NumberConstraintCondition.class, NumberConstraintCondition.CONVERTER)
            .register(OrCondition.class, OrCondition.CONVERTER)
            .register(RandomChanceCondition.class, RandomChanceCondition.CONVERTER)
            .register(ReferenceCondition.class, ReferenceCondition.CONVERTER)
            .register(SurvivesExplosionCondition.class, SurvivesExplosionCondition.CONVERTER)
            .register(TimeCheckCondition.class, TimeCheckCondition.CONVERTER)
            .register(ToolCheckCondition.class, ToolCheckCondition.CONVERTER)
            .register(WeatherCheckCondition.class, WeatherCheckCondition.CONVERTER)
            .register(ConstantNumber.class, ConstantNumber.CONVERTER)
            .register(BinomialNumber.class, BinomialNumber.CONVERTER)
            .register(UniformNumber.class, UniformNumber.CONVERTER)
            .register(ContextNBT.class, ContextNBT.CONVERTER)
            .register(StorageNBT.class, StorageNBT.CONVERTER)
            .register(BonusCountModifier.BinomialBonus.class, BonusCountModifier.BinomialBonus.CONVERTER)
            .register(BonusCountModifier.UniformBonus.class, BonusCountModifier.UniformBonus.CONVERTER)
            .register(BonusCountModifier.FortuneDrops.class, BonusCountModifier.FortuneDrops.CONVERTER)
            .build();

    /**
     * The default TypeSerializerCollection for Trove. This includes {@link FieldTypes#STANDARD_TYPES},
     * {@link MinestomTypes#STANDARD_TYPES},
     */
    public static final @NotNull TypeSerializerCollection DEFAULT_COLLECTION =
            TypeSerializerCollection.builder()
                    .registerExact(LootEntry.class, TroveMinestom.createEntryBuilder().build())
                    .registerExact(LootModifier.class, TroveMinestom.createModifierBuilder().build())
                    .registerExact(LootCondition.class, TroveMinestom.createConditionBuilder().build())
                    .registerExact(LootNumber.class, TroveMinestom.createNumberBuilder().build())
                    .registerExact(LootNBT.class, TroveMinestom.createNbtBuilder().build())
                    .register(VanillaInterface.EntityPredicate.class, FieldTypes.join(
                                (input, result) -> {},
                                input -> (world, location, entity) -> false
                        ))
                    .register(VanillaInterface.LocationPredicate.class, FieldTypes.join(
                                (input, result) -> {},
                                input -> (world, location) -> false
                        ))
                    .register(LootContextKeyGroup.class, FieldTypes.join(
                                (input, result) -> result.set(input.id()),
                                input -> {
                                    var id = input.getString();
                                    if (id == null) {
                                        throw new SerializationException(input, LootContextKeyGroup.class, "Expected a string");
                                    }
                                    return STANDARD_GROUPS.get(id);
                                }
                        ))
                    .registerAll(FieldTypes.STANDARD_TYPES)
                    .registerAll(MinestomTypes.STANDARD_TYPES)
                    .registerAll(TroveMinestom.INSTANCE_COLLECTION)
                    .build();

    /**
     * The default vanilla interface implementation for {@link dev.goldenstack.loot.minestom.context.LootContextKeys#VANILLA_INTERFACE}.
     */
    public static final @NotNull VanillaInterface DEFAULT_INTERFACE = new FallbackVanillaInterface() {};

    /**
     * Parses every JSON file in the provided directory, or one of its subdirectories, into loot tables, returning the
     * results in to a table registry instance.
     * @param directory the directory to parse
     * @return the registry instance that contains parsing information
     */
    public static @NotNull TableRegistry readTables(@NotNull Path directory, @NotNull Supplier<GsonConfigurationLoader.Builder> builderSupplier) {
        Map<NamespaceID, LootTable> tables = new HashMap<>();
        Map<NamespaceID, ConfigurateException> exceptions = new HashMap<>();

        final String FILE_SUFFIX = ".json";

        List<Path> files;
        try (var stream = Files.find(directory, Integer.MAX_VALUE,
                (path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(FILE_SUFFIX))) {
            files = stream.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // For now just return the tables and ignore if there are no problems.
        for (var path : files) {
            String keyPath = StreamSupport.stream(directory.relativize(path).spliterator(), false).map(Path::toString).collect(Collectors.joining("/"));

            if (keyPath.endsWith(FILE_SUFFIX)) { // Should be true, but let's check it anyway
                keyPath = keyPath.substring(0, keyPath.length() - FILE_SUFFIX.length());
            }

            NamespaceID key = NamespaceID.from(keyPath);

            var root = builderSupplier.get().source(() -> Files.newBufferedReader(path)).build();
            try {
                tables.put(key, root.load().get(LootTable.class));
            } catch (ConfigurateException exception) {
                exceptions.put(key, exception);
            }
        }

        return new TableRegistry(tables, exceptions);
    }

    /**
     * Holds information about some parsed loot tables, including the actual parsed tables and any potential exceptions
     * that occurred while parsing them.
     * @param tables the map of key to parsed loot table
     * @param exceptions the map of key to the error that occurred while parsing the loot table
     */
    public record TableRegistry(@NotNull Map<NamespaceID, LootTable> tables,
                                @NotNull Map<NamespaceID, ConfigurateException> exceptions) {

        /**
         * Gets the parsed loot table at the provided key.
         * @param key the key to look for a table at
         * @return the loot table at the provided id, or null if there is not one
         */
        public @Nullable LootTable getTable(@NotNull NamespaceID key) {
            return tables.get(key);
        }

        /**
         * Gets the parsed loot table associated with the provided key, or {@link LootTable#EMPTY} if there is not one.
         * @param key the key to look for a table at
         * @return the loot table at the provided id, or empty if there is not one
         */
        public @NotNull LootTable getTableOrEmpty(@NotNull NamespaceID key) {
            return tables.getOrDefault(key, LootTable.EMPTY);
        }

        /**
         * Gets the parsed loot table at the provided key, throwing an exception that involves the failed parsing if
         * an exception fitting that description could be found.
         * @param key the key to look for a table at
         * @return the loot table at the provided id
         */
        public @NotNull LootTable requireTable(@NotNull NamespaceID key) {
            var table = tables.get(key);

            if (table != null) {
                return table;
            } else if (exceptions.containsKey(key)) {
                throw new IllegalArgumentException("Table with key '" + key + "' could not be parsed", exceptions.get(key));
            } else {
                throw new IllegalArgumentException("Unknown table key '" + key + "'");
            }
        }

    }

    public static @NotNull LootConversionManager<LootEntry> createEntryBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootEntry.class))
                .keyLocation("type")
                .add(AlternativeEntry.KEY, AlternativeEntry.class)
                .add(DynamicEntry.KEY, DynamicEntry.class)
                .add(EmptyEntry.KEY, EmptyEntry.class)
                .add(GroupEntry.KEY, GroupEntry.class)
                .add(ItemEntry.KEY, ItemEntry.class)
                .add(SequenceEntry.KEY, SequenceEntry.class)
                .add(TableEntry.KEY, TableEntry.class)
                .add(TagEntry.KEY, TagEntry.class);
    }

    public static @NotNull LootConversionManager<LootModifier> createModifierBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootModifier.class))
                .keyLocation("function")
                .add(ApplyLootingModifier.KEY, ApplyLootingModifier.class)
                .add(BonusCountModifier.KEY, BonusCountModifier.class)
                .add(CopyNameModifier.KEY, CopyNameModifier.class)
                .add(CopyNbtModifier.KEY, CopyNbtModifier.class)
                .add(CopyStateModifier.KEY, CopyStateModifier.class)
                .add(ExplosionDecayModifier.KEY, ExplosionDecayModifier.class)
                .add(LevelledEnchantModifier.KEY, LevelledEnchantModifier.class)
                .add(LimitCountModifier.KEY, LimitCountModifier.class)
                .add(RandomlyEnchantModifier.KEY, RandomlyEnchantModifier.class)
                .add(SetAttributesModifier.KEY, SetAttributesModifier.class)
                .add(SetContentsModifier.KEY, SetContentsModifier.class)
                .add(SetCountModifier.KEY, SetCountModifier.class)
                .add(SetDamageModifier.KEY, SetDamageModifier.class)
                .add(SetNbtModifier.KEY, SetNbtModifier.class)
                .add(SetPotionModifier.KEY, SetPotionModifier.class)
                .add(SetStewEffectModifier.KEY, SetStewEffectModifier.class)
                .add(SmeltItemModifier.KEY, SmeltItemModifier.class);
    }

    public static @NotNull LootConversionManager<LootCondition> createConditionBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootCondition.class))
                .keyLocation("condition")
                .add(AndCondition.KEY, AndCondition.class)
                .add(BlockStateCondition.KEY, BlockStateCondition.class)
                .add(EnchantmentLevelCondition.KEY, EnchantmentLevelCondition.class)
                .add(EntityCheckCondition.KEY, EntityCheckCondition.class)
                .add(InvertedCondition.KEY, InvertedCondition.class)
                .add(KilledByPlayerCondition.KEY, KilledByPlayerCondition.class)
                .add(LocationCheckCondition.KEY, LocationCheckCondition.class)
                .add(LootingRandomChanceCondition.KEY, LootingRandomChanceCondition.class)
                .add(NumberConstraintCondition.KEY, NumberConstraintCondition.class)
                .add(OrCondition.KEY, OrCondition.class)
                .add(RandomChanceCondition.KEY, RandomChanceCondition.class)
                .add(ReferenceCondition.KEY, ReferenceCondition.class)
                .add(SurvivesExplosionCondition.KEY, SurvivesExplosionCondition.class)
                .add(TimeCheckCondition.KEY, TimeCheckCondition.class)
                .add(ToolCheckCondition.KEY, ToolCheckCondition.class)
                .add(WeatherCheckCondition.KEY, WeatherCheckCondition.class);
    }

    public static @NotNull LootConversionManager<LootNumber> createNumberBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootNumber.class))
                .keyLocation("type")
                .add(ConstantNumber.ACCURATE_CONVERTER)
                .add(ConstantNumber.KEY, ConstantNumber.class)
                .add(BinomialNumber.KEY, BinomialNumber.class)
                .add(UniformNumber.KEY, UniformNumber.class);
    }

    public static @NotNull LootConversionManager<LootNBT> createNbtBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootNBT.class))
                .keyLocation("type")
                .add(ContextNBT.ACCURATE_CONVERTER)
                .add(ContextNBT.KEY, ContextNBT.class)
                .add(StorageNBT.KEY, StorageNBT.class);
    }

}
