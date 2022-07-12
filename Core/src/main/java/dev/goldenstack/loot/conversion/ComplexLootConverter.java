package dev.goldenstack.loot.conversion;

import dev.goldenstack.loot.context.LootConversionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Handles complex conversion where nothing is guaranteed about provided objects.
 * @param <L> the loot item
 * @param <T> the class of the object that will be provided
 */
public abstract class ComplexLootConverter<L, T extends LootAware<L>> implements LootConverter<L, T> {

    /**
     * @param node the node that is being checked
     * @param context the context object that stores extra data
     * @return true if this converter should be used to deserialize the provided node
     */
    public abstract boolean canDeserialize(@Nullable ConfigurationNode node, @NotNull LootConversionContext<L> context);

    /**
     * @param node the node to deserialize
     * @param context the context for this deserialization
     * @return the instance of {@link T} that was created
     * @throws ConfigurateException if, for some reason, something goes wrong while deserializing
     */
    public abstract @NotNull T deserialize(@Nullable ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException;

    /**
     * @param input the input, which is the object to check
     * @param context the context object that stores extra data
     * @return true if this converter should be used to serialize the provided input
     */
    public abstract boolean canSerialize(@NotNull T input, @NotNull LootConversionContext<L> context);

    /**
     * @param input the input that needs to be serialized
     * @param context the context for this serialization
     * @return the configuration node that was created
     * @throws ConfigurateException if, for some reason, something goes wrong while serializing
     */
    public abstract @NotNull ConfigurationNode serialize(@NotNull T input, @NotNull LootConversionContext<L> context) throws ConfigurateException;

}
