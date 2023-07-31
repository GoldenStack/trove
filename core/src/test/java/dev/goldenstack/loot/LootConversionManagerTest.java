package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.generator.LootConversionManager;
import dev.goldenstack.loot.converter.TypedLootConverter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
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

        builder.add("x", (TypedLootConverter<? extends A>) (Object) emptySerializer(X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateKey() {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.add("b", emptySerializer(B.class, B::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateType() {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.add("b", emptySerializer(A.class, A::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testActiveConditionalSerializer() throws ConfigurateException {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        builder.add(emptyConditionalSerializer(B::new, true, true));

        var manager = builder.build();

        var node = node();
        manager.serialize(new A(), node);

        assertEquals(B.class, handle(manager, "location", "a").getClass());
        assertEquals(node(null), node);
    }

    @Test
    public void testInactiveConditionalSerializer() throws ConfigurateException {
        var builder = new LootConversionManager<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", emptySerializer(A.class, A::new));
        builder.add(emptyConditionalSerializer(B::new, false, false));

        var manager = builder.build();

        var node = node();
        manager.serialize(new A(), node);

        assertEquals(A.class, handle(manager, "location", "a").getClass());
        assertEquals(node(Map.of("location", "a")), node);
    }

    private static <O> @NotNull O handle(@NotNull TypedLootConverter<O> deserializer, @NotNull String keyLocation, @NotNull String keyValue) throws ConfigurateException {
        return deserializer.deserialize(node(Map.of(keyLocation, keyValue)));
    }

}