package dev.goldenstack.loot.minestom.nbt;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.namespaceId;

/**
 * Returns the NBT compound contained in the
 * {@link dev.goldenstack.loot.minestom.VanillaInterface#getCommandStorageValue(NamespaceID) data storage} under the key
 * {@link #storageKey()}.
 * @param storageKey the key to get the associated value of in the data storage
 */
public record StorageNBT(@NotNull NamespaceID storageKey) implements LootNBT {

    /**
     * A standard map-based converter for storage NBT providers.
     */
    public static final @NotNull KeyedLootConverter<StorageNBT> CONVERTER =
            converter(StorageNBT.class,
                    namespaceId().name("storageKey").nodePath("source")
            ).keyed("minecraft:storage");

    @Override
    public @NotNull NBT getNBT(@NotNull LootGenerationContext context) {
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

        var nbt = vanilla.getCommandStorageValue(storageKey);
        return nbt != null ? nbt : new NBTCompound();
    }

}
