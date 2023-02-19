package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConditionalConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class TestUtils {
    private TestUtils() {}

    public static @NotNull ImmuTables emptyLoader() {
        return new ImmuTables(null, null, null, null, BasicConfigurationNode.factory()::createNode);
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

    public static @NotNull LootConversionContext emptyConversionContext() {
        return new LootConversionContext(emptyLoader(), Map.of());
    }

    public static @NotNull LootConversionContext conversionContext(@NotNull Map<LootContext.Key<?>, Object> information) {
        return new LootConversionContext(emptyLoader(), information);
    }

    public static @NotNull LootGenerationContext generationContext(@NotNull Map<LootContext.Key<?>, Object> information) {
        return new LootGenerationContext(new Random(), information);
    }

    public static <V> @NotNull AdditiveConditionalConverter<V> emptyConditionalSerializer(@NotNull Supplier<V> initializer,
                                                                                          boolean canSerialize,
                                                                                          boolean canDeserialize) {
        return new AdditiveConditionalConverter<>() {
            @Override
            public boolean canSerialize(@NotNull V input, @NotNull LootConversionContext context) {
                return canSerialize;
            }

            @Override
            public boolean canDeserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) {
                return canDeserialize;
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) {
                return initializer.get();
            }

            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {

            }
        };
    }

    public static <V> @NotNull KeyedLootConverter<V> emptyKeyedSerializer(@NotNull String key,
                                                                                @NotNull Class<V> convertedType,
                                                                                @NotNull Supplier<V> initializer) {
        return new KeyedLootConverter<>(key, TypeToken.get(convertedType)) {
            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) {
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) {
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
