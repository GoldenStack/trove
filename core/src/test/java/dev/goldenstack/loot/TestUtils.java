package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.serialize.generator.FieldTypes;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

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

    public static <V> @NotNull TypeSerializer<V> serializer(@Nullable Object serialize, @Nullable V deserialize) {
        return FieldTypes.join(
                (input, result) -> result.set(serialize),
                input -> deserialize
        );
    }

    public static <V> @NotNull TypeSerializer<V> emptySerializer(@NotNull Supplier<V> initializer) {
        return FieldTypes.join((input, result) -> {}, input -> initializer.get());
    }

}
