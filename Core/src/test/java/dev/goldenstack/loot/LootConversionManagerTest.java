package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.converter.meta.ConditionalLootConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.converter.meta.LootConversionManager;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class LootConversionManagerTest {

    static class A {}
    static class B extends A {}

    static class X {}

    @Test
    public void testBuilderSubtyping(){
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(createKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter(createKeyedSerializer("b", B.class, B::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter((KeyedLootConverter<String, ? extends A>) (Object) createKeyedSerializer("x", X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testBaseTypeBypass() {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(createKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        var builder2 = ((LootConversionManager.Builder<String, X>) (Object) builder).baseType(new TypeToken<>(){});

        builder2.addConverter(createKeyedSerializer("x", X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateKey() {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(createKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter(createKeyedSerializer("a", B.class, B::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateType() {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(createKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter(createKeyedSerializer("b", A.class, A::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testActiveConditionalSerializer() throws ConfigurateException {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(createKeyedSerializer("a", A.class, A::new));
        builder.addInitialConverter(createConditionalSerializer(B::new, true, true));

        var manager = builder.build();

        assertEquals(B.class, handle(manager, "a").getClass());
        assertEquals(nodeOf(null), handle(manager, new A()));
    }

    @Test
    public void testInactiveConditionalSerializer() throws ConfigurateException {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(createKeyedSerializer("a", A.class, A::new));
        builder.addInitialConverter(createConditionalSerializer(B::new, false, false));

        var manager = builder.build();

        assertEquals(A.class, handle(manager, "a").getClass());
        assertEquals(nodeOf(Map.of("location", "a")), handle(manager, new A()));
    }

    private static @NotNull ConfigurationNode nodeOf(@Nullable Object object) throws SerializationException {
        return BasicConfigurationNode.factory().createNode().set(object);
    }

    private static <L, O> @NotNull ConfigurationNode handle(@NotNull LootConversionManager<L, O> serializer, @NotNull O input) throws ConfigurateException {
        ImmuTables<L> loader = ImmuTables.<L>builder().nodeProducer(BasicConfigurationNode.factory()::createNode).build();
        return serializer.serialize(input, LootConversionContext.<L>builder().loader(loader).build());
    }

    private static <L, O> @NotNull O handle(@NotNull LootConversionManager<L, O> deserializer, @Nullable String keyValue) throws ConfigurateException {
        ImmuTables<L> loader = ImmuTables.<L>builder().nodeProducer(BasicConfigurationNode.factory()::createNode).build();
        var node = loader.createNode();
        if (keyValue != null) {
            node.node(deserializer.keyLocation()).set(keyValue);
        }
        return deserializer.deserialize(node, LootConversionContext.<L>builder().loader(loader).build());
    }

    private static <L, V> @NotNull ConditionalLootConverter<L, V> createConditionalSerializer(@NotNull Supplier<V> initializer,
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

    private static <L, V> @NotNull KeyedLootConverter<L, V> createKeyedSerializer(@NotNull String key,
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

}