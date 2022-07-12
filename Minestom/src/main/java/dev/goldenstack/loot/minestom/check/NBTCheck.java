package dev.goldenstack.loot.minestom.check;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.minestom.util.Utils;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.StringReader;

/**
 * Essentially just a wrapper for {@link Utils#compareNBT(NBT, NBT, boolean)}
 * @param nbt the NBT that will always act as the
 */
public record NBTCheck(@Nullable NBTCompound nbt) {

    /**
     * @param check the NBT check to serialize
     * @param context the (currently unused in this method) context
     * @return the provided check as a JSON element
     * @throws ConfigurateException if something goes wrong while serializing
     */
    public static @NotNull ConfigurationNode serialize(@NotNull NBTCheck check, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        ConfigurationNode node = context.loader().createNode();
        if (check.nbt() != null) {
            node.set(check.nbt().toSNBT());
        }
        return node;
    }

    /**
     * @param node the node to attempt to deserialize
     * @param context the (currently unused in this method) context
     * @return the element parsed into NBT if possible and wrapped with {@link NBTCheck}
     * @throws ConfigurateException if the provided element was neither null nor a JSON primitive or if it was a
     *                                 primitive and it could not be parsed into a valid NBT compound
     */
    public static @NotNull NBTCheck deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        if (node.empty()) {
            return new NBTCheck(null);
        }
        String string = node.getString();
        if (string == null) {
            throw new ConfigurateException(node, "Expected the node value to be a string");
        }
        try {
            NBT nbt = new SNBTParser(new StringReader(string)).parse();

            if (!(nbt instanceof NBTCompound compound)) {
                throw new ConfigurateException(node, "Expected the provided SNBT to parse into a NBTCompound");
            }

            return new NBTCheck(compound);

        } catch (NBTException e) {
            throw new ConfigurateException("Could not parse NBT: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * @param nbt the nbt to compare against this check's NBT
     * @return the result of {@link Utils#compareNBT(NBT, NBT, boolean)} with this check's NBT as the guarantee, the
     *         provided NBT as the comparison, and assureListOrder as false
     */
    public boolean test(@Nullable NBT nbt) {
        return Utils.compareNBT(this.nbt, nbt, false);
    }
}
