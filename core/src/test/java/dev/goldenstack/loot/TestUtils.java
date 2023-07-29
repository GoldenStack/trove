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

import java.util.Optional;
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
        return ConditionalLootConverter.join(
                (input, result, context) -> {
                    if (canSerialize) {
                        result.set(null);
                    }
                }, (input, context) -> Optional.ofNullable(canDeserialize ? initializer.get() : null)
        );
    }

    public static <V> @NotNull TypedLootConverter<V> emptySerializer(@NotNull Class<V> convertedType,
                                                                     @NotNull Supplier<V> initializer) {
        return TypedLootConverter.join(convertedType, (input, result, context) -> {}, (input, context) -> initializer.get());
    }

}
