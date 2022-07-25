package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.ConditionalLootConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class TestUtils {
    private TestUtils() {}

    public static <L> @NotNull ImmuTables<L> emptyLoader() {
        return new ImmuTables<>(null, null, null, null, null, null, BasicConfigurationNode.factory()::createNode);
    }

    public static @NotNull LootContext context(@NotNull Map<LootContext.Key<?>, Object> information) {
        return new LootContextImpl(information);
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

    public static <L> @NotNull LootConversionContext<L> emptyConversionContext() {
        return new LootConversionContext<>(emptyLoader(), Map.of());
    }

    public static <L> @NotNull LootConversionContext<L> conversionContext(@NotNull Map<LootContext.Key<?>, Object> information) {
        return new LootConversionContext<>(emptyLoader(), information);
    }

    public static @NotNull LootGenerationContext generationContext(@NotNull Map<LootContext.Key<?>, Object> information) {
        return new LootGenerationContext(new Random(), information);
    }

    public static <L, V> @NotNull ConditionalLootConverter<L, V> emptyConditionalSerializer(@NotNull Supplier<V> initializer,
                                                                                            boolean canSerialize,
                                                                                            boolean canDeserialize) {
        return new ConditionalLootConverter<>() {
            @Override
            public boolean canSerialize(@NotNull V input, @NotNull LootConversionContext<L> context) {
                return canSerialize;
            }

            @Override
            public boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context) {
                return canDeserialize;
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context) {
                return initializer.get();
            }

            @Override
            public @NotNull ConfigurationNode serialize(@NotNull V input, @NotNull LootConversionContext<L> context) {
                return context.loader().createNode();
            }
        };
    }

    public static <L, V> @NotNull KeyedLootConverter<L, V> emptyKeyedSerializer(@NotNull String key,
                                                                                @NotNull Class<V> convertedType,
                                                                                @NotNull Supplier<V> initializer) {
        return new KeyedLootConverter<>(key, TypeToken.get(convertedType)) {
            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<L> context) {
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context) {
                return initializer.get();
            }
        };
    }

    private record LootContextImpl(@NotNull Map<Key<?>, Object> information) implements LootContext {
        public LootContextImpl {
            information = Map.copyOf(information);
        }
    }


}
