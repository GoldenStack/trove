package dev.goldenstack.loot.converter;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Optional;

/**
 * A loot converter that may optionally serialize and deserialize its input.
 * @param <V> the converted type
 */
public interface ConditionalLootConverter<V> extends LootSerializer<V>, LootDeserializer<Optional<V>> {

    /**
     * Joins the provided serializer, and deserializer into a new ConditionalLootConverter.
     * @param serializer the serializer to use
     * @param deserializer the deserializer to use
     * @return a conditional converter joining the provided type and converter
     * @param <V> the converted type
     */
    static <V> @NotNull ConditionalLootConverter<V> join(@NotNull LootSerializer<V> serializer, @NotNull LootDeserializer<Optional<V>> deserializer) {
        return new ConditionalLootConverter<>() {
            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result) throws SerializationException {
                serializer.serialize(input, result);
            }

            @Override
            public @NotNull Optional<V> deserialize(@NotNull ConfigurationNode input) throws SerializationException {
                return deserializer.deserialize(input);
            }
        };
    }

}
