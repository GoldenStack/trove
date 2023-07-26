package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.meta.LootConversionManager;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
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
        var builder = LootConversionManager.<A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter("b", emptySerializer(B.class, B::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter("x", (TypedLootConverter<? extends A>) (Object) emptySerializer(X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testBaseTypeBypass() {
        var builder = LootConversionManager.<A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        var builder2 = ((LootConversionManager.Builder<X>) (Object) builder).baseType(new TypeToken<>(){});

        builder2.addConverter("x", emptySerializer(X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateKey() {
        var builder = LootConversionManager.<A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter("b", emptySerializer(B.class, B::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateType() {
        var builder = LootConversionManager.<A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter("a", emptySerializer(A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter("b", emptySerializer(A.class, A::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testActiveConditionalSerializer() throws ConfigurateException {
        var builder = LootConversionManager.<A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter("a", emptySerializer(A.class, A::new));
        builder.addInitialConverter(emptyConditionalSerializer(B::new, true, true));

        var manager = builder.build();

        var node = node();
        manager.serialize(new A(), node, Trove.builder().build());

        assertEquals(B.class, handle(manager, "location", "a").getClass());
        assertEquals(node(null), node);
    }

    @Test
    public void testInactiveConditionalSerializer() throws ConfigurateException {
        var builder = LootConversionManager.<A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter("a", emptySerializer(A.class, A::new));
        builder.addInitialConverter(emptyConditionalSerializer(B::new, false, false));

        var manager = builder.build();

        var node = node();
        manager.serialize(new A(), node, Trove.builder().build());

        assertEquals(A.class, handle(manager, "location", "a").getClass());
        assertEquals(node(Map.of("location", "a")), node);
    }

    private static <O> @NotNull O handle(@NotNull LootConversionManager<O> deserializer, @NotNull String keyLocation, @NotNull String keyValue) throws ConfigurateException {
        return deserializer.deserialize(node(Map.of(keyLocation, keyValue)), Trove.builder().build());
    }

}