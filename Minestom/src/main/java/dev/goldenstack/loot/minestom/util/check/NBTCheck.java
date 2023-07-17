package dev.goldenstack.loot.minestom.util.check;

import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.minestom.util.nbt.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.nbt;

/**
 * A standard predicate for NBT elements.
 */
public record NBTCheck(@Nullable NBT guarantee) {

    public static final @NotNull AdditiveConverter<NBTCheck> CONVERTER =
            converter(NBTCheck.class,
                    nbt().name("guarantee").nodePath("nbt").optional()
            ).additive();

    /**
     * Creates a new NBT predicate that checks the provided element.
     */
    public static @NotNull NBTCheck of(@Nullable NBT element) {
        return new NBTCheck(element);
    }

    /**
     * Checks whether or not the provided element passes this predicate.
     * @param element the element to check
     * @return true if the provided NBT passes the check
     */
    public boolean verify(@Nullable NBT element) {
        return guarantee == null || NBTUtils.compareNBT(guarantee, element, false);
    }

}
