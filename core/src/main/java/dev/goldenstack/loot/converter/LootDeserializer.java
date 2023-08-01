package dev.goldenstack.loot.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * A standard representation of something that can deserialize a configuration node into a specific type of object.
 * @param <V> the type of output that will be created
 */
public interface LootDeserializer<V> {

    /**
     * Deserializes an input node into a resulting object.<br>
     * Note: it is possible for the provided node to not have a value.
     * @param input the configuration node to deserialize into an instance of {@link V}
     * @return the provided configuration node deserialized into an object
     * @throws SerializationException if the input could not be deserialized for some reason
     */
    @Nullable V deserialize(@NotNull ConfigurationNode input) throws SerializationException;

}
