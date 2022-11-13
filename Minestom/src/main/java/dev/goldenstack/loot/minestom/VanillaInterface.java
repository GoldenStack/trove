package dev.goldenstack.loot.minestom;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to the features of vanilla Minecraft that are required for specific loot-related features, not forcing
 * any specific implementation.
 * Default implementations of these methods will throw {@link UnsupportedOperationException}.
 */
public interface VanillaInterface {

    /**
     * @param instance the instance to check
     * @return true if the provided instance is raining
     */
    default boolean isRaining(@NotNull Instance instance) {
        throw new UnsupportedOperationException("VanillaInterface#isRaining has not been implemented!");
    }

    /**
     * @param instance the instance to check
     * @return true if the provided instance is thundering
     */
    default boolean isThundering(@NotNull Instance instance) {
        throw new UnsupportedOperationException("VanillaInterface#isThundering has not been implemented!");
    }

    /**
     * @param entity the entity to check
     * @return the level of looting of the provided entity
     */
    default int getLooting(@NotNull Entity entity) {
        throw new UnsupportedOperationException("VanillaInterface#getLooting has not been implemented!");
    }
}
