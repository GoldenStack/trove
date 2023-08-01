package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.converter.generator.FieldTypes;
import dev.goldenstack.loot.converter.generator.LootConversionManager;
import dev.goldenstack.loot.converter.TypedLootConverter;
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


    /**
     * The default TypeSerializerCollection for Trove. This includes {@link FieldTypes#STANDARD_TYPES},
     * {@link MinestomTypes#STANDARD_TYPES},
     */
    public static final @NotNull TypeSerializerCollection DEFAULT_COLLECTION =
            TypeSerializerCollection.builder()
                    .register(LootEntry.class, TroveMinestom.createEntryBuilder().build())
                    .register(LootModifier.class, TroveMinestom.createModifierBuilder().build())
                    .register(LootCondition.class, TroveMinestom.createConditionBuilder().build())
                    .register(LootNumber.class, TroveMinestom.createNumberBuilder().build())
                    .register(LootNBT.class, TroveMinestom.createNbtBuilder().build())
                    .register(VanillaInterface.EntityPredicate.class, TypedLootConverter.join(VanillaInterface.EntityPredicate.class,
                                (input, result) -> {},
                                input -> (world, location, entity) -> false
                        ))
                    .register(VanillaInterface.LocationPredicate.class, TypedLootConverter.join(VanillaInterface.LocationPredicate.class,
                                (input, result) -> {},
                                input -> (world, location) -> false
                        ))
                    .register(LootContextKeyGroup.class, TypedLootConverter.join(LootContextKeyGroup.class,
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
                .add(AlternativeEntry.KEY, AlternativeEntry.CONVERTER)
                .add(DynamicEntry.KEY, DynamicEntry.CONVERTER)
                .add(EmptyEntry.KEY, EmptyEntry.CONVERTER)
                .add(GroupEntry.KEY, GroupEntry.CONVERTER)
                .add(ItemEntry.KEY, ItemEntry.CONVERTER)
                .add(SequenceEntry.KEY, SequenceEntry.CONVERTER)
                .add(TableEntry.KEY, TableEntry.CONVERTER)
                .add(TagEntry.KEY, TagEntry.CONVERTER);
    }

    public static @NotNull LootConversionManager<LootModifier> createModifierBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootModifier.class))
                .keyLocation("function")
                .add(ApplyLootingModifier.KEY, ApplyLootingModifier.CONVERTER)
                .add(BonusCountModifier.KEY, BonusCountModifier.CONVERTER)
                .add(CopyNameModifier.KEY, CopyNameModifier.CONVERTER)
                .add(CopyNbtModifier.KEY, CopyNbtModifier.CONVERTER)
                .add(CopyStateModifier.KEY, CopyStateModifier.CONVERTER)
                .add(ExplosionDecayModifier.KEY, ExplosionDecayModifier.CONVERTER)
                .add(LevelledEnchantModifier.KEY, LevelledEnchantModifier.CONVERTER)
                .add(LimitCountModifier.KEY, LimitCountModifier.CONVERTER)
                .add(RandomlyEnchantModifier.KEY, RandomlyEnchantModifier.CONVERTER)
                .add(SetAttributesModifier.KEY, SetAttributesModifier.CONVERTER)
                .add(SetContentsModifier.KEY, SetContentsModifier.CONVERTER)
                .add(SetCountModifier.KEY, SetCountModifier.CONVERTER)
                .add(SetDamageModifier.KEY, SetDamageModifier.CONVERTER)
                .add(SetNbtModifier.KEY, SetNbtModifier.CONVERTER)
                .add(SetPotionModifier.KEY, SetPotionModifier.CONVERTER)
                .add(SetStewEffectModifier.KEY, SetStewEffectModifier.CONVERTER)
                .add(SmeltItemModifier.KEY, SmeltItemModifier.CONVERTER);
    }

    public static @NotNull LootConversionManager<LootCondition> createConditionBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootCondition.class))
                .keyLocation("condition")
                .add(AndCondition.KEY, AndCondition.CONVERTER)
                .add(BlockStateCondition.KEY, BlockStateCondition.CONVERTER)
                .add(EnchantmentLevelCondition.KEY, EnchantmentLevelCondition.CONVERTER)
                .add(EntityCheckCondition.KEY, EntityCheckCondition.CONVERTER)
                .add(InvertedCondition.KEY, InvertedCondition.CONVERTER)
                .add(KilledByPlayerCondition.KEY, KilledByPlayerCondition.CONVERTER)
                .add(LocationCheckCondition.KEY, LocationCheckCondition.CONVERTER)
                .add(LootingRandomChanceCondition.KEY, LootingRandomChanceCondition.CONVERTER)
                .add(NumberConstraintCondition.KEY, NumberConstraintCondition.CONVERTER)
                .add(OrCondition.KEY, OrCondition.CONVERTER)
                .add(RandomChanceCondition.KEY, RandomChanceCondition.CONVERTER)
                .add(ReferenceCondition.KEY, ReferenceCondition.CONVERTER)
                .add(SurvivesExplosionCondition.KEY, SurvivesExplosionCondition.CONVERTER)
                .add(TimeCheckCondition.KEY, TimeCheckCondition.CONVERTER)
                .add(ToolCheckCondition.KEY, ToolCheckCondition.CONVERTER)
                .add(WeatherCheckCondition.KEY, WeatherCheckCondition.CONVERTER);
    }

    public static @NotNull LootConversionManager<LootNumber> createNumberBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootNumber.class))
                .keyLocation("type")
                .add(ConstantNumber.ACCURATE_CONVERTER)
                .add(ConstantNumber.KEY, ConstantNumber.CONVERTER)
                .add(BinomialNumber.KEY, BinomialNumber.CONVERTER)
                .add(UniformNumber.KEY, UniformNumber.CONVERTER);
    }

    public static @NotNull LootConversionManager<LootNBT> createNbtBuilder() {
        return new LootConversionManager<>(TypeToken.get(LootNBT.class))
                .keyLocation("type")
                .add(ContextNBT.ACCURATE_CONVERTER)
                .add(ContextNBT.KEY, ContextNBT.CONVERTER)
                .add(StorageNBT.KEY, StorageNBT.CONVERTER);
    }

}
