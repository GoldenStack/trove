package dev.goldenstack.loot.converter;

import dev.goldenstack.loot.context.LootConversionContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A standard representation of something that can serialize a specific type of object into a configuration node.
 * @param <L> the loot item type
 * @param <I> the type of input that is required for serialization
 */
public interface LootSerializer<L, I> {

    /**
     * Serializes an input object into a resulting node.<br>
     * Note: it is valid for the returned node to not have a value.
     * @param input the input object to serialize into a configuration node
     * @param context the context object, to use if required
     * @return the provided object serialized into a configuration node
     * @throws ConfigurateException if the input could not be serialized for some reason
     */
    @NotNull ConfigurationNode serialize(@NotNull I input, @NotNull LootConversionContext<L> context) throws ConfigurateException;

}
