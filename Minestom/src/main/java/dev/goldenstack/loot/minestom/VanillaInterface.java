package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Predicate;

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

    /**
     * Enchants an item with the provided RNG, levels, and treasure permission, and returns the enchanted item.
     * @param random the random instance to use for generation
     * @param item the item to enchant
     * @param levels the number of levels to enchant the item with
     * @param permitTreasure whether or not treasure enchantments can potentially be added from this enchanting
     * @return the enchanted item
     */
    default @NotNull ItemStack enchantItem(@NotNull Random random, @NotNull ItemStack item, int levels, boolean permitTreasure) {
        throw new UnsupportedOperationException("VanillaInterface#enchantItem has not been implemented!");
    }

    /**
     * Attempts to smelt an item. If the item cannot be smelted, or its recipe is invalid, or if there's anything else
     * preventing a valid item from being returned, the result should be null.
     * @param item the item to smelt
     * @return the result of the smelting, or null if it could not be smelted for some reason
     */
    default @Nullable ItemStack smeltItem(@NotNull ItemStack item) {
        throw new UnsupportedOperationException("VanillaInterface#smeltItem has not been implemented!");
    }

    /**
     * Provides the converter for item predicates. These are the predicates that Minecraft uses for items in a variety
     * of circumstances.
     * @return the converter for item predicates
     */
    default @NotNull AdditiveConverter<Predicate<ItemStack>> itemPredicateConverter() {
        throw new UnsupportedOperationException("VanillaInterface#itemPredicateConverter has not been implemented!");
    }
}
