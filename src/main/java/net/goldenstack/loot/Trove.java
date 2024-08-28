package net.goldenstack.loot;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Trove {
    
    private Trove() {}
    
    /**
     * Parses every JSON file in the provided directory, or one of its subdirectories, into loot tables, returning the
     * results in to a table registry instance.
     * @param directory the directory to parse
     * @return the registry instance that contains parsing information
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull Map<NamespaceID, LootTable> readTables(@NotNull Path directory) {
        Map<NamespaceID, LootTable> tables = new HashMap<>();

        final String FILE_SUFFIX = ".json";

        List<Path> files;
        try (var stream = Files.find(directory, Integer.MAX_VALUE,
                (path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(FILE_SUFFIX))) {
            files = stream.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (var path : files) {

            String keyPath = StreamSupport.stream(directory.relativize(path).spliterator(), false).map(Path::toString).collect(Collectors.joining("/"));

            if (!keyPath.endsWith(FILE_SUFFIX)) continue;
            keyPath = keyPath.substring(0, keyPath.length() - FILE_SUFFIX.length());

            BinaryTag tag;
            try {
                tag = TagStringIOExt.readTag(Files.readString(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            tables.put(
                    NamespaceID.from(keyPath),
                    LootTable.SERIALIZER.read(new BinaryTagSerializer.ContextWithRegistries(MinecraftServer.process()), tag)
            );
        }

        return tables;
    }
    
}
