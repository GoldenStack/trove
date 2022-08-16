package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.context.LootConversionContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A loot serializer that, instead of creating its own object, adds properties onto the provided object.
 * @param <L> the loot item type
 * @param <V> the type being serialized
 */
public interface AdditiveLootSerializer<L, V> {

    /**
     * Serializes an input object into a resulting node.<br>
     * Note: it is valid, but not recommended, for the resulting node to be set to a non-map (including null) value.
     * @param input the input object to serialize onto the node
     * @param result the configuration node to serialize values onto. Importantly, this node might already have values.
     * @param context the context object, to use if required
     * @throws ConfigurateException if the input could not be serialized on to the provided object for some reason
     */
    void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<L> context) throws ConfigurateException;

}
