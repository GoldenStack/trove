package dev.goldenstack.loot.converter;

import dev.goldenstack.loot.context.LootConversionContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Has the ability to serialize and deserialize items of a specific type.
 * @param <V> the type to serialize and deserialize
 */
public interface LootConverter<V> extends LootSerializer<V>, LootDeserializer<V> {

    /**
     * Joins the provided serializer and deserializer into a LootConverter instance.
     * @param serializer the new converter's serializer
     * @param deserializer the new converter's deserializer
     * @return a converter that joins the provided instances
     * @param <V> the type to convert
     */
    static <V> @NotNull LootConverter<V> join(@NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<V> deserializer) {
        return new LootConverter<>() {
            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
                serializer.serialize(input, result, context);
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
                return deserializer.deserialize(input, context);
            }
        };
    }

}
