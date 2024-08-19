package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VanillaInterface {

    @Nullable Integer getScore(@NotNull Entity entity, @NotNull String objective);

    @NotNull BinaryTag serializeEntity(@NotNull Entity entity);

}
