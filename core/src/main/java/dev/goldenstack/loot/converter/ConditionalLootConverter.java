package dev.goldenstack.loot.converter;

import dev.goldenstack.loot.Trove;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

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
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull Trove context) throws ConfigurateException {
                serializer.serialize(input, result, context);
            }

            @Override
            public @NotNull Optional<V> deserialize(@NotNull ConfigurationNode input, @NotNull Trove context) throws ConfigurateException {
                return deserializer.deserialize(input, context);
            }
        };
    }

}
