package dev.goldenstack.loot.context;

import dev.goldenstack.loot.ImmuTables;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class LootContextTest {

    @Test
    public void testImplementationMapImmutability() {
        var key = new LootContext.Key<>("example_string", TypeToken.get(String.class));

        var conversion = LootConversionContext.builder()
                .loader(new ImmuTables<>())
                .addInformation(key, "value")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> conversion.information().clear());
        assertThrows(UnsupportedOperationException.class, () -> conversion.information().put(key, "new_value"));
        assertEquals("value", conversion.information().get(key));

        var generation = LootGenerationContext.builder()
                .random(new Random())
                .addInformation(key, "value")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> generation.information().clear());
        assertThrows(UnsupportedOperationException.class, () -> generation.information().put(key, "new_value"));
        assertEquals("value", generation.information().get(key));
    }

    @Test
    public void testDefaultImplementations() {
        var key1 = new LootContext.Key<>("key1", TypeToken.get(String.class));
        var key2 = new LootContext.Key<>("key2", TypeToken.get(String.class));

        var context = new LootContextImpl(Map.of(key1, "value"));

        assertEquals(Map.of(key1, "value"), context.information());

        assertTrue(context.has(key1));
        assertFalse(context.has(key2));

        assertEquals("value", context.get(key1));
        assertNull(context.get(key2));

        assertEquals("value", context.assure(key1));
        assertThrows(NoSuchElementException.class, () -> context.assure(key2));
    }

    @Test
    public void testDifferentTypes() {
        var type1 = new LootContext.Key<>("type", TypeToken.get(String.class));
        var type2 = new LootContext.Key<>("type", TypeToken.get(Integer.class));

        var context = new LootContextImpl(Map.of(type1, "value"));

        assertTrue(context.has(type1));
        assertFalse(context.has(type2));

        assertEquals("value", context.get(type1));
        assertNull(context.get(type2));

        assertEquals("value", context.assure(type1));
        assertThrows(NoSuchElementException.class, () -> context.assure(type2));

    }

    private record LootContextImpl(@NotNull Map<LootContext.Key<?>, Object> information) implements LootContext {
        public LootContextImpl {
            information = Map.copyOf(information);
        }
    }
}
