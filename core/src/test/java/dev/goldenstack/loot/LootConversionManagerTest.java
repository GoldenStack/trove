package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.generator.LootConversionManager;
import dev.goldenstack.loot.converter.TypedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

import java.util.Map;

import static dev.goldenstack.loot.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class LootConversionManagerTest {

    static class A {}
    static class B extends A {}

    static class X {}

    @Test
    public void testBuilderSubtyping(){
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.add("b", emptySerializer(B.class, B::new));
        assertDoesNotThrow(builder::build);

        assertThrows(IllegalArgumentException.class, () -> builder.add("x", (TypedLootConverter<? extends A>) (Object) emptySerializer(X.class, X::new)));
    }

    @Test
    public void testDuplicateKey() {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        assertThrows(IllegalArgumentException.class, () -> builder.add("a", emptySerializer(B.class, B::new)));
    }

    @Test
    public void testDuplicateType() {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        assertThrows(IllegalArgumentException.class, () -> builder.add("b", emptySerializer(A.class, A::new)));
    }

    @Test
    public void testActiveConditionalSerializer() throws ConfigurateException {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        builder.add(converter("empty", new B()));

        var manager = builder.build();

        var node = node();
        manager.serialize(A.class, new A(), node);

        assertInstanceOf(B.class, handle(manager, "location", "a"));
        assertEquals(node("empty"), node);
    }

    @Test
    public void testInactiveConditionalSerializer() throws ConfigurateException {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        builder.add(converter(null, null));

        var manager = builder.build();

        var node = node();
        manager.serialize(A.class, new A(), node);

        assertInstanceOf(A.class, handle(manager, "location", "a"));
        assertEquals(node(Map.of("location", "a")), node);
    }

    private static <O> @Nullable O handle(@NotNull TypedLootConverter<O> deserializer, @NotNull String keyLocation, @NotNull String keyValue) throws ConfigurateException {
        return deserializer.deserialize(deserializer.convertedType().getType(), node(Map.of(keyLocation, keyValue)));
    }

}