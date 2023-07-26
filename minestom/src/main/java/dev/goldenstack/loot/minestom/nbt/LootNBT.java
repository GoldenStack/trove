package dev.goldenstack.loot.minestom.nbt;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;

/**
 * Returns some NBT data based on internal state and the provided context.
 */
public interface LootNBT {

    /**
     * Generates some NBT based on internal state and the provided context.
     * @param context the context to use for NBT
     * @return the NBT, or null if there is none
     */
    @Nullable NBT getNBT(@NotNull LootContext context);

}
