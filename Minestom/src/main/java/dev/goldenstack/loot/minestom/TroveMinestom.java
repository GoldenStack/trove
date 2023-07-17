package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.Trove;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.util.FallbackVanillaInterface;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.goldenstack.loot.minestom.context.LootContextKeyGroup.*;

/**
 * Provides utilities for easily reading loot tables.
 */
public class TroveMinestom {

    /**
     * The default Trove instance that contains all of the default information for loading loot tables.
     */
    public static final @NotNull Trove DEFAULT_LOADER = MinestomLoader.initializeBuilder(
            Trove.builder().nodeProducer(BasicConfigurationNode.factory()::createNode)
    ).build();

    /**
     * The default vanilla interface implementation for {@link dev.goldenstack.loot.minestom.context.LootContextKeys#VANILLA_INTERFACE}.
     */
    public static final @NotNull VanillaInterface DEFAULT_INTERFACE = new FallbackVanillaInterface() {};

    /**
     * The standard map of key groups for {@link dev.goldenstack.loot.minestom.context.LootConversionKeys#CONTEXT_KEYS}.
     */
    public static final @NotNull Map<String, LootContextKeyGroup> STANDARD_GROUPS = Stream.of(
            EMPTY, CHEST, COMMAND, SELECTOR, FISHING, ENTITY, GIFT, BARTER, ADVANCEMENT_REWARD, ADVANCEMENT_ENTITY, GENERIC, BLOCK
    ).collect(Collectors.toMap(LootContextKeyGroup::id, Function.identity()));

    /**
     * Reads a loot table from the provided path.
     * @param path the path to read from
     * @param context the context to provide to the parser
     * @return the parsed loot table
     * @throws ConfigurateException if a loot table could not be read from the provided path
     */
    public static @NotNull LootTable readTable(@NotNull Path path, @NotNull LootConversionContext context) throws ConfigurateException {
        var root = GsonConfigurationLoader.builder().source(() -> Files.newBufferedReader(path)).build().load();
        return LootTable.CONVERTER.deserialize(root, context);
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
                throw new IllegalArgumentException("A table with key '" + "' key was found, but could not be parsed", exceptions.get(key));
            } else {
                throw new IllegalArgumentException("A table with key '" + key + "' could not be found!");
            }
        }

    }

    /**
     * Parses every JSON file in the provided directory, or one of its subdirectories, into loot tables, returning the
     * results in to a table registry instance.
     * @param directory the directory to parse
     * @param context the context to use for parsing
     * @return the registry instance that contains parsing information
     */
    public static @NotNull TableRegistry readTables(@NotNull Path directory, @NotNull LootConversionContext context) {
        Map<NamespaceID, LootTable> tables = new HashMap<>();
        Map<NamespaceID, ConfigurateException> exceptions = new HashMap<>();

        List<Path> files;
        try (var stream = Files.find(directory, Integer.MAX_VALUE,
                (path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(".json"))) {
            files = stream.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // For now just return the tables and ignore if there are no problems.
        for (var path : files) {
            String keyPath = StreamSupport.stream(directory.relativize(path).spliterator(), false).map(Path::toString).collect(Collectors.joining("/"));
            NamespaceID key = NamespaceID.from(keyPath);

            try {
                tables.put(key, readTable(path, context));
            } catch (ConfigurateException exception) {
                exceptions.put(key, exception);
            }
        }

        return new TableRegistry(tables, exceptions);
    }

}
