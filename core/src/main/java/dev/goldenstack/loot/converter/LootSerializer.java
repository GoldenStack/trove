package dev.goldenstack.loot.converter;

import dev.goldenstack.loot.Trove;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A standard representation of something that can serialize a specific type of object onto a configuration node.
 * @param <V> the type being serialized
 */
public interface LootSerializer<V> {

    /**
     * Serializes the provided input object onto the provided result node.
     * @param input the input instance to serialize
     * @param result the result node to serialize the input onto
     * @param context the context object to use for serialization
     * @throws ConfigurateException if the input could not be serialized onto the result object for some reason
     */
    void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException;

}
