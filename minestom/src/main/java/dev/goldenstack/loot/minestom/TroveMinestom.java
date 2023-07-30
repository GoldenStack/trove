package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.converter.meta.LootConversionManager;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
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
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.lang.reflect.Type;
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
     * The standard typed converters that either have not been implemented (so the placeholders are here) or are
     * otherwise somewhat dynamic.
     */
    public static final @NotNull List<TypedLootConverter<?>> STANDARD_CONVERTERS = List.of(
            TroveMinestom.createEntryBuilder().build(),
            TroveMinestom.createModifierBuilder().build(),
            TroveMinestom.createConditionBuilder().build(),
            TroveMinestom.createNumberBuilder().build(),
            TroveMinestom.createNbtBuilder().build(),
            TypedLootConverter.join(VanillaInterface.EntityPredicate.class,
                    (input, result) -> {},
                    input -> (world, location, entity) -> false
            ),
            TypedLootConverter.join(VanillaInterface.LocationPredicate.class,
                    (input, result) -> {},
                    input -> (world, location) -> false
            ),
            TypedLootConverter.join(LootContextKeyGroup.class,
                    (input, result) -> result.set(input.id()),
                    input -> {
                        var id = input.getString();
                        if (id == null) {
                            throw new SerializationException(input, LootContextKeyGroup.class, "Expected a string");
                        }
                        return STANDARD_GROUPS.get(id);
                    }
            )
    );

    /**
     * The default collection for Trove.
     */
    public static final @NotNull TypeSerializerCollection DEFAULT_COLLECTION = TroveMinestom.wrap(STANDARD_CONVERTERS);

    /**
     * The default vanilla interface implementation for {@link dev.goldenstack.loot.minestom.context.LootContextKeys#VANILLA_INTERFACE}.
     */
    public static final @NotNull VanillaInterface DEFAULT_INTERFACE = new FallbackVanillaInterface() {};

    /**
     * Reads a loot table from the provided input.
     * @param input the input node to parse
     * @return the parsed loot table
     * @throws ConfigurateException if a loot table could not be read from the provided node
     */
    public static @NotNull LootTable readTable(@NotNull ConfigurationNode input) throws ConfigurateException {
        return LootTable.CONVERTER.deserialize(input);
    }

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
                tables.put(key, readTable(root.load()));
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

    /**
     * Wraps the provided list of converters in a new type serializer collection.
     * @param converters the converters to wrap in a type serializer collection
     * @return a type serializer collection representing each provided converter
     */
    public static @NotNull TypeSerializerCollection wrap(@NotNull List<TypedLootConverter<?>> converters) {
        var builder = TypeSerializerCollection.builder();
        for (var converter : converters) {
            add(converter, builder);
        }
        return builder.build();
    }

    /**
     * Wraps the provided converter in a new type serializer.
     * @param converter the converter to convert to a type serializer
     * @return a type serializer that uses the provided converter
     * @param <V> the converted type
     */
    public static <V> @NotNull TypeSerializer<V> wrap(@NotNull TypedLootConverter<V> converter) {
        return new TypeSerializer<>() {
            @Override
            public V deserialize(Type type, ConfigurationNode node) throws SerializationException {
                return converter.deserialize(node);
            }

            @Override
            public void serialize(Type type, @Nullable V obj, ConfigurationNode node) throws SerializationException {
                if (obj == null) {
                    throw new SerializationException(node, converter.convertedType().getType(), "Cannot serialize null object");
                }
                converter.serialize(obj, node);
            }
        };
    }

    private static <V> void add(@NotNull TypedLootConverter<V> converter, @NotNull TypeSerializerCollection.Builder builder) {
        builder.register(converter.convertedType(), wrap(converter));
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
