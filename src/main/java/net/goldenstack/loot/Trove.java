package net.goldenstack.loot;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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
    public static @NotNull Map<Key, LootTable> readTables(@NotNull Path directory) {
        Map<Key, LootTable> tables = new HashMap<>();

        final String FILE_SUFFIX = ".json";

        List<Path> files;
        try (var stream = Files.find(directory, Integer.MAX_VALUE,
                (path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(FILE_SUFFIX))) {
            files = stream.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());

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
                    Key.key(keyPath),
                    LootTable.CODEC.decode(coder, tag).orElseThrow("parsing " + path)
            );
        }

        return tables;
    }

    public static void blockDrop(@NotNull Instance instance, @NotNull ItemStack item, @NotNull Point block) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        Pos spawn = new Pos(
                block.blockX() + 0.5 + rng.nextDouble(-0.25, 0.25),
                block.blockY() + 0.5 + rng.nextDouble(-0.25, 0.25) - EntityType.ITEM.height() / 2,
                block.blockZ() + 0.5 + rng.nextDouble(-0.25, 0.25),
                rng.nextFloat(360),
                0
        );

        drop(instance, item, spawn);
    }

    public static void drop(@NotNull Instance instance, @NotNull ItemStack item, @NotNull Point position) {
        ItemEntity entity = new ItemEntity(item);

        ThreadLocalRandom rng = ThreadLocalRandom.current();

        Vec vel = new Vec(
                rng.nextDouble(-0.1, 0.1),
                0.2,
                rng.nextDouble(-0.1, 0.1)
        ).mul(20);

        entity.setPickupDelay(10, TimeUnit.SERVER_TICK);

        entity.setInstance(instance, position);
        entity.setVelocity(vel);
    }

}
