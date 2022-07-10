package dev.goldenstack.loot.minestom.check;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.minestom.util.Utils;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

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
     * @throws LootConversionException if something goes wrong while serializing
     */
    public static @NotNull JsonElement serialize(@NotNull NBTCheck check, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        return check.nbt() == null ? JsonNull.INSTANCE : new JsonPrimitive(check.nbt().toSNBT());
    }

    /**
     * @param element the element to attempt to deserialize
     * @param context the (currently unused in this method) context
     * @return the element parsed into NBT if possible and wrapped with {@link NBTCheck}
     * @throws LootConversionException if the provided element was neither null nor a JSON primitive or if it was a
     *                                 primitive and it could not be parsed into a valid NBT compound
     */
    public static @NotNull NBTCheck deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        if (JsonUtils.isNull(element)) {
            return new NBTCheck(null);
        }
        String string = JsonUtils.assureJsonPrimitive(element, null).getAsString();
        try {
            NBT nbt = new SNBTParser(new StringReader(string)).parse();

            if (!(nbt instanceof NBTCompound compound)) {
                throw new LootConversionException("Expected the provided SNBT to parse into a NBTCompound");
            }

            return new NBTCheck(compound);

        } catch (NBTException e) {
            throw new LootConversionException("Could not parse NBT: " + e.getLocalizedMessage(), e);
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
