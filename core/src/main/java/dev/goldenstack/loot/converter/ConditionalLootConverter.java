package dev.goldenstack.loot.converter;

import dev.goldenstack.loot.Trove;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A loot converter that has the ability to communicate whether or not each of its individual actions (i.e.,
 * serialization and deserialization) should occur.<br>
 * Note: the behaviour of both {@link #serialize(Object, ConfigurationNode, Trove)} and
 * {@link #deserialize(ConfigurationNode, Trove)} is undefined when their respective conditional methods
 * do not return true for the provided input.
 * @param <V> the type of object that will be serialized and deserialized
 */
public interface ConditionalLootConverter<V> extends LootConverter<V> {

    /**
     * Determines whether or not the provided input can be serialized by this converter.
     * @param input the input object that will be checked
     * @param loader the loader object, to use if required
     * @return true if this converter can be used to deserialize the provided input
     */
    boolean canSerialize(@NotNull V input, @NotNull Trove loader);

    /**
     * Determines whether or not the provided input can be deserialized by this converter.
     * @param input the input object that will be checked
     * @param loader the loader object, to use if required
     * @return true if this converter can be used to deserialize the provided input
     */
    boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull Trove loader);

}
