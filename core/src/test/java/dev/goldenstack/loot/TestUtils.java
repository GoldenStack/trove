package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.ConditionalLootConverter;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class TestUtils {
    private TestUtils() {}

    public static <T> LootContext.@NotNull Key<T> key(@NotNull String name, @NotNull Class<T> type) {
        return new LootContext.Key<>(name, TypeToken.get(type));
    }

    public static @NotNull ConfigurationNode node() {
        return BasicConfigurationNode.factory().createNode();
    }

    public static @NotNull ConfigurationNode node(@Nullable Object object) {
        try {
            return node().set(object);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> @NotNull ConditionalLootConverter<V> emptyConditionalSerializer(@NotNull Supplier<V> initializer,
                                                                                      boolean canSerialize,
                                                                                      boolean canDeserialize) {
        return new ConditionalLootConverter<>() {
            @Override
            public boolean canSerialize(@NotNull V input, @NotNull Trove context) {
                return canSerialize;
            }

            @Override
            public boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull Trove context) {
                return canDeserialize;
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull Trove context) {
                return initializer.get();
            }

            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull Trove context) {

            }
        };
    }

    public static <V> @NotNull TypedLootConverter<V> emptySerializer(@NotNull Class<V> convertedType,
                                                                     @NotNull Supplier<V> initializer) {
        return TypedLootConverter.join(convertedType, (input, result, context) -> {}, (input, context) -> initializer.get());
    }

}
