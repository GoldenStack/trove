package dev.goldenstack.loot;

import dev.goldenstack.loot.serialize.generator.SerializerSelector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.Map;

import static dev.goldenstack.loot.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class SerializerSelectorTest {

    static class A {}
    static class B extends A {}

    static class X {}

    @Test
    public void testBuilderSubtyping(){
        var builder = new SerializerSelector<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", A.class);
        assertDoesNotThrow(builder::build);

        builder.add("b", B.class);
        assertDoesNotThrow(builder::build);

        assertThrows(IllegalArgumentException.class, () -> builder.add("x", (Class<? extends A>) (Object) X.class));
    }

    @Test
    public void testDuplicateKey() {
        var builder = new SerializerSelector<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", A.class);
        assertDoesNotThrow(builder::build);

        assertThrows(IllegalArgumentException.class, () -> builder.add("a", A.class));
    }

    @Test
    public void testDuplicateType() {
        var builder = new SerializerSelector<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", A.class);
        assertDoesNotThrow(builder::build);

        assertThrows(IllegalArgumentException.class, () -> builder.add("b", A.class));
    }

    @Test
    public void testActiveConditionalSerializer() throws ConfigurateException {
        var builder = new SerializerSelector<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add("a", A.class);
        builder.add(serializer("empty", new B()));

        var selector = builder.build();

        var node = nodeWith(selector).set(A.class, new A());
        assertEquals(node("empty"), node);

        assertDoesNotThrow(() -> nodeWith(selector).set(Map.of("location", "a")).require(B.class));
    }

    @Test
    public void testInactiveConditionalSerializer() throws ConfigurateException {
        var builder = new SerializerSelector<>(TypeToken.get(A.class))
                .keyLocation("location");

        builder.add(serializer(null, null));
        builder.add("b", B.class);

        var selector = builder.build();

        var node = nodeWith(selector).set(A.class, new B());
        assertEquals(node(Map.of("location", "b")), node);

        assertDoesNotThrow(() -> nodeWith(selector).set(Map.of("location", "b")).require(B.class));
    }

    private static @NotNull ConfigurationNode nodeWith(@NotNull TypeSerializer<A> serializer) {
        return BasicConfigurationNode.root(ConfigurationOptions.defaults().serializers(b ->
                b.register(B.class, emptySerializer(B::new)).registerExact(A.class, serializer)
        ));
    }

}