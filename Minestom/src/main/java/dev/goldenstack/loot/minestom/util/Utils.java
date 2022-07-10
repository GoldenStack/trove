package dev.goldenstack.loot.minestom.util;

import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

/**
 * General utilities used for this library.
 */
public class Utils {
    private Utils() {}

    /**
     * Checks to see if everything in {@code guarantee} is contained in {@code comparison}. Because of how this works,
     * the comparison is allowed to have extra fields that are not contained in the guarantee.
     * @param guarantee the guarantee that the comparison must have all elements of
     * @param comparison the comparison, that is being compared against the guarantee. NBT compounds in this parameter,
     *                   whether deeper in the tree or not, are allowed to have keys that the guarantee does not - it's
     *                   basically compared against a standard.
     * @param assureListOrder whether or not to assure list order. When true, lists are directly compared, but when
     *                        false, the comparison is checked to see if it contains each item in the guarantee.
     * @return true if the comparison fits the guarantee, otherwise false
     */
    public static boolean compareNBT(@Nullable NBT guarantee, @Nullable NBT comparison, boolean assureListOrder) {
        if (guarantee == null) {
            // If there's no guarantee, it must always pass
            return true;
        } else if (comparison == null) {
            // If it's null at this point, we already assured that guarantee is null, so it must be invalid
            return false;
        } else if (!guarantee.getClass().equals(comparison.getClass())) {
            // If the classes aren't equal it can't fulfill the guarantee anyway
            return false;
        }
        // If the list order is assured, it will be handled with the simple #equals call later in the method
        if (!assureListOrder && guarantee instanceof NBTList<?> guaranteeList) {
            NBTList<?> comparisonList = ((NBTList<?>) comparison);
            if (guaranteeList.isEmpty()) {
                return comparisonList.isEmpty();
            }
            for (NBT nbt : guaranteeList) {
                boolean contains = false;
                for (NBT compare : comparisonList) {
                    if (compareNBT(nbt, compare, false)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    return false;
                }
            }
            return true;
        }

        if (guarantee instanceof NBTCompound guaranteeCompound) {
            NBTCompound comparisonCompound = ((NBTCompound) comparison);
            for (String key : guaranteeCompound.getKeys()) {
                if (!compareNBT(guaranteeCompound.get(key), comparisonCompound.get(key), assureListOrder)) {
                    return false;
                }
            }
            return true;
        }

        return guarantee.equals(comparison);
    }
}
