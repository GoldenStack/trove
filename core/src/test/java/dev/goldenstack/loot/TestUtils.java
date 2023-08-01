package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.TypedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class TestUtils {
    private TestUtils() {}

    public static <T> @NotNull LootContext context(@NotNull LootContext.Key<T> key, @NotNull T value) {
        return LootContext.builder().random(new Random(0)).with(key, value).build();
    }

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

    public static <V> @NotNull LootConverter<V> converter(@Nullable Object serialize, @Nullable V deserialize) {
        return LootConverter.join(
                (input, result) -> result.set(serialize),
                input -> deserialize
        );
    }

    public static <V> @NotNull TypedLootConverter<V> emptySerializer(@NotNull Class<V> convertedType,
                                                                     @NotNull Supplier<V> initializer) {
        return TypedLootConverter.join(convertedType, (input, result) -> {}, input -> initializer.get());
    }

}
