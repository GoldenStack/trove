package dev.goldenstack.loot.minestom;

import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An interface to the features of vanilla Minecraft that are required for specific loot-related features, not forcing
 * any specific implementation.
 * Default implementations of these methods will throw {@link UnsupportedOperationException}.
 */
public interface VanillaInterface {

    /**
     * Verifies that the provided location in the world fits some predicate.
     */
    interface LocationPredicate {

        boolean test(@NotNull Instance world, @NotNull Point location);

    }

    /**
     * Verifies that the provided entity, world, and position fit some predicate.
     */
    interface EntityPredicate {

        boolean test(@NotNull Instance world, @Nullable Point location, @Nullable Entity entity);
    }

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
     * Returns whether or not the provided item is allowed to be enchanted with the provided enchantment.
     * @param item the item to check
     * @param enchantment the enchantment that could potentially be applied
     * @return true if the enchantment can be applied, false if not
     */
    default boolean canApplyEnchantment(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        throw new UnsupportedOperationException("VanillaInterface#canApplyEnchantment has not been implemented!");
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
     * Provides the converter for location predicates. These are the predicates that Minecraft uses for locations, such
     * as for advancements and loot tables.
     * @return the converter for location predicates
     */
    default @NotNull AdditiveConverter<LocationPredicate> locationPredicateConverter() {
        throw new UnsupportedOperationException("VanillaInterface#locationPredicateConverter has not been implemented!");
    }

    /**
     * Provides the converter for entity predicates. These are the predicates that Minecraft uses for entities, such as
     * in loot tables and various game mechanics.
     * @return the converter for entities
     */
    default @NotNull AdditiveConverter<EntityPredicate> entityPredicateConverter() {
        throw new UnsupportedOperationException("VanillaInterface#entityPredicateConverter has not been implemented!");
    }

    /**
     * Converts the provided entity into an NBT compound representing it.
     * @param entity the entity to convert to NBT
     * @return NBT representing the entity
     */
    default @NotNull NBTCompound getEntityNBT(@NotNull Entity entity) {
        throw new UnsupportedOperationException("VanillaInterface#getEntityNBT has not been implemented!");
    }

    /**
     * Retrieves the value in command storage that is stored under the provided key. Implementation-wise, this is
     * allowed to return null if there is nothing stored under the key, or it may return a default; both are equally
     * valid.
     * @param key the key to query
     * @return the value stored in command storage at the provided key
     */
    default @Nullable NBTCompound getCommandStorageValue(@NotNull NamespaceID key) {
        throw new UnsupportedOperationException("VanillaInterface#getCommandStorageValue has not been implemented!");
    }

    /**
     * Returns the map that provides dynamic drop calculators based on each provided ID. This is used in
     * {@link dev.goldenstack.loot.minestom.entry.DynamicEntry}.
     * @return the dynamic drop provider map
     */
    default @NotNull Map<NamespaceID, Function<NBTCompound, List<ItemStack>>> getDynamicDropProvider() {
        throw new UnsupportedOperationException("VanillaInterface#getDynamicDropProvider has not been implemented!");
    }

}
