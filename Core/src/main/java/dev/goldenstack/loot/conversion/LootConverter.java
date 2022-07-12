package dev.goldenstack.loot.conversion;

import dev.goldenstack.loot.context.LootConversionContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * A generic converter that is open to any type.
 * @param <L> the loot item
 * @param <T> the type to serialize & deserialize
 */
public interface LootConverter<L, T> {

    @NotNull ConfigurationNode serialize(@NotNull T input, @NotNull LootConversionContext<L> context) throws ConfigurateException;

    @NotNull T deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException;

}
