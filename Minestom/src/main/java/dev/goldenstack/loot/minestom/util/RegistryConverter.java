package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.function.Function;

/**
 * Converts something as simple as a registry value.
 * @param serializer the T -> string converter
 * @param deserializer the string -> T converter
 * @param <L> the loot item
 * @param <T> the converted type
 */
public record RegistryConverter<L, T> (@NotNull Function<T, String> serializer,
                                       @NotNull Function<String, T> deserializer) implements LootConverter<L, T> {
    @Override
    public @NotNull ConfigurationNode serialize(@NotNull T input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        return context.loader().createNode().set(serializer.apply(input));
    }

    public @NotNull ConfigurationNode serializeNullable(@Nullable T input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        return input == null ? context.loader().createNode() : serialize(input, context);
    }

    @Override
    public @NotNull T deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        String value = node.getString();
        if (value == null) {
            throw new ConfigurateException(node, "Expected the value of the node to be a string, but found another type");
        }
        T t = deserializer.apply(value);
        if (t == null) {
            throw new ConfigurateException(node, "Invalid registry value '" + value + "'");
        }
        return t;
    }

    public @Nullable T deserializeNullable(@NotNull ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException {
        String value = node.getString();
        if (value == null) {
            return null;
        }
        T t = deserializer.apply(value);
        if (t == null) {
            throw new ConfigurateException(node, "Invalid registry value '" + value + "'");
        }
        return t;
    }
}
