package dev.goldenstack.loot.minestom.nbt;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.type;

/**
 * Returns the NBT compound contained in the
 * {@link dev.goldenstack.loot.minestom.VanillaInterface#getCommandStorageValue(NamespaceID) data storage} under the key
 * {@link #storageKey()}.
 * @param storageKey the key to get the associated value of in the data storage
 */
public record StorageNBT(@NotNull NamespaceID storageKey) implements LootNBT {

    public static final @NotNull String KEY = "minecraft:storage";

    /**
     * A standard map-based converter for storage NBT providers.
     */
    public static final @NotNull TypedLootConverter<StorageNBT> CONVERTER =
            converter(StorageNBT.class,
                    type(NamespaceID.class).name("storageKey").nodePath("source")
            );

    @Override
    public @NotNull NBT getNBT(@NotNull LootContext context) {
        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

        var nbt = vanilla.getCommandStorageValue(storageKey);
        return nbt != null ? nbt : new NBTCompound();
    }

}
