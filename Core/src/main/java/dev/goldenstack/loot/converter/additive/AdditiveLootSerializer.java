package dev.goldenstack.loot.converter.additive;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.LootSerializer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A loot serializer that, instead of creating its own object, adds properties onto the provided object.
 * @param <V> the type being serialized
 */
public interface AdditiveLootSerializer<V> extends LootSerializer<V> {

    /**
     * Serializes an input object into a resulting node.<br>
     * Note: it is valid, but not recommended, for the resulting node to be set to a non-map (including null) value.
     * @param input the input object to serialize onto the node
     * @param result the configuration node to serialize values onto. Importantly, this node might already have values.
     * @param context the context object, to use if required
     * @throws ConfigurateException if the input could not be serialized onto the provided object for some reason
     */
    void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException;

    /**
     * A default implementation of serialize, simply creating a new node and calling
     * {@link #serialize(Object, ConfigurationNode, LootConversionContext)} on it.<br>
     * {@inheritDoc}
     */
    @Override
    default @NotNull ConfigurationNode serialize(@NotNull V input, @NotNull LootConversionContext context) throws ConfigurateException {
        var result = context.loader().createNode();
        serialize(input, result, context);
        return result;
    }

}
