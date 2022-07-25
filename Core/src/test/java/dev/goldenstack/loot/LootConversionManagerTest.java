package dev.goldenstack.loot;

import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.converter.meta.LootConversionManager;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

import java.util.Map;

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

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter(TestUtils.emptyKeyedSerializer("b", B.class, B::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter((KeyedLootConverter<String, ? extends A>) (Object) TestUtils.emptyKeyedSerializer("x", X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testBaseTypeBypass() {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        var builder2 = ((LootConversionManager.Builder<String, X>) (Object) builder).baseType(new TypeToken<>(){});

        builder2.addConverter(TestUtils.emptyKeyedSerializer("x", X.class, X::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateKey() {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", B.class, B::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testDuplicateType() {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", A.class, A::new));
        assertDoesNotThrow(builder::build);

        builder.addConverter(TestUtils.emptyKeyedSerializer("b", A.class, A::new));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testActiveConditionalSerializer() throws ConfigurateException {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", A.class, A::new));
        builder.addInitialConverter(TestUtils.emptyConditionalSerializer(B::new, true, true));

        var manager = builder.build();

        assertEquals(B.class, handle(manager, "a").getClass());
        assertEquals(TestUtils.node(null), manager.serialize(new A(), TestUtils.emptyConversionContext()));
    }

    @Test
    public void testInactiveConditionalSerializer() throws ConfigurateException {
        var builder = LootConversionManager.<String, A>builder()
                .baseType(new TypeToken<>(){})
                .keyLocation("location");

        builder.addConverter(TestUtils.emptyKeyedSerializer("a", A.class, A::new));
        builder.addInitialConverter(TestUtils.emptyConditionalSerializer(B::new, false, false));

        var manager = builder.build();

        assertEquals(A.class, handle(manager, "a").getClass());
        assertEquals(TestUtils.node(Map.of("location", "a")), manager.serialize(new A(), TestUtils.emptyConversionContext()));
    }

    private static <L, O> @NotNull O handle(@NotNull LootConversionManager<L, O> deserializer, @NotNull String keyValue) throws ConfigurateException {
        return deserializer.deserialize(TestUtils.node(Map.of(deserializer.keyLocation(), keyValue)), TestUtils.emptyConversionContext());
    }

}