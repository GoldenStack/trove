package dev.goldenstack.loot.converter.meta;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.LootConverter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A loot converter that has the ability to communicate whether or not each of its individual actions (i.e.,
 * serialization and deserialization) should occur with a simple method.<br>
 * Note: the behaviour of both {@link #serialize(Object, LootConversionContext)} and
 * {@link #deserialize(ConfigurationNode, LootConversionContext)} is undefined when their respective conditional
 * methods do not return true for the provided input.
 * @param <L> the loot item type
 * @param <V> the type of object that will be serialized and deserialized
 */
public interface ConditionalLootConverter<L, V> extends LootConverter<L, V> {

    /**
     * A method to test if {@link #serialize(Object, LootConversionContext)} should be called on the provided input.
     * @param input the input object to test for if it should be serialized
     * @param context the context object, to use if required
     * @return true if this converter should be used to serialize the provided input, and false if otherwise
     */
    boolean canSerialize(@NotNull V input, @NotNull LootConversionContext<L> context);

    /**
     * A method to test if {@link #deserialize(ConfigurationNode, LootConversionContext)} should be called on the provided input.
     * @param input the configuration node to test for if it should be deserialized
     * @param context the context object, to use if required
     * @return true if this converter should be used to deserialize the provided input, and false if otherwise
     */
    boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context);

}
