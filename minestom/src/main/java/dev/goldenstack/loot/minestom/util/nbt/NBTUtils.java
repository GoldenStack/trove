package dev.goldenstack.loot.minestom.util.nbt;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains various NBT-related utilities
 */
public class NBTUtils {

    /**
     * Checks to see if everything in {@code guarantee} is contained in {@code comparison}. The comparison is allowed to
     * have extra fields that are not contained in the guarantee.
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

    /**
     * Merges the two provided compounds, preferring the value of the {@code changes} compound and merging any nested
     * NBT compounds like it would for the first-level ones.
     * @param base the base compound, to be merged onto
     * @param changes the changes to make to the base compound
     * @return the merged compound
     */
    public static NBTCompound merge(@NotNull NBTCompound base, @NotNull NBTCompound changes) {
        return base.modify(mutable -> {
            for (var entry : changes) {
                var value = (mutable.get(entry.getKey()) instanceof NBTCompound baseCompound &&
                        entry.getValue() instanceof NBTCompound changeCompound) ?
                        merge(baseCompound, changeCompound) : entry.getValue();

                mutable.set(entry.getKey(), value);
            }
        });
    }

    /**
     * Reads a NBT compound from the provided reader.<br>
     * This implementation may be slow, as it may try to parse NBT many times, but this is unavoidable for now.
     */
    public static @Nullable NBTCompound readCompoundSNBT(@NotNull StringReader reader) throws IOException {
        if (reader.read() != '{') {
            return null;
        }
        StringBuilder string = new StringBuilder("{");

        while (true) {
            // Since this is a compound we should always read to at least the next closing curly brackets. However we
            // can't count brackets and skip to until we think they should be valid because they could be escaped.

            int next;
            do {
                next = reader.read();

                if (next == -1) {
                    return null;
                }

                string.appendCodePoint(next);
            } while (next != '}');

            try {
                var nbt = new SNBTParser(new StringReader(string.toString())).parse();
                return nbt instanceof NBTCompound compound ? compound : null;
            } catch (NBTException ignored) {}

        }

    }

    /**
     * Converts the provided list of items into a NBTList of them, each containing the item NBT and the slot index.
     * @param items the items to convert to NBT
     * @return the NBT list representing the items
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull NBTList<NBTCompound> itemsToList(@NotNull List<ItemStack> items) {
        List<NBTCompound> compounds = new ArrayList<>();

        for(int i = 0; i < items.size(); ++i) {
            ItemStack item = items.get(i);
            if (!item.isAir()) {
                byte slotIndex = (byte) i;
                var itemNBT = item.toItemNBT().modify(mut -> mut.setByte("Slot", slotIndex));

                compounds.add(itemNBT);
            }
        }

        return new NBTList<>(NBTType.TAG_Compound, compounds);
    }

    /**
     * Converts the provided NBT list into a list of items, using the byte stored under the {@code Slot} key in each
     * compound as its index, but falling back to its actual index in the list if it does not have the slot stored.
     * @param itemNBT the NBT list representing the items
     * @return the items converted from NBT
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull List<ItemStack> listToItems(@NotNull NBTList<NBTCompound> itemNBT) {
        int itemCount = 0;
        for (var compound : itemNBT) {
            Byte slot = compound.getByte("Slot");
            itemCount = Math.max(itemCount, slot != null ? slot : 0);
        }

        List<ItemStack> items = new ArrayList<>(itemCount);
        Collections.fill(items, ItemStack.AIR);

        // Iterate with a normal loop so we can use the index as a fallback.
        for (int i = 0; i < itemNBT.getSize(); i++) {
            Byte slot = itemNBT.get(i).getByte("Slot");

            var item = ItemStack.fromItemNBT(itemNBT.get(i));
            items.set(slot != null ? slot : i, item);
        }

        return items;
    }

}
