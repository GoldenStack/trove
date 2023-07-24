package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ResultOfMethodCallIgnored", "AssertBetweenInconvertibleTypes"})
public class LootContextTest {

    @Test
    public void testImplementationMapImmutability() {
        var key = TestUtils.key("example_string", String.class);

        var conversion = TestUtils.conversionContext(Map.of(key, "value"));

        assertThrows(UnsupportedOperationException.class, () -> conversion.information().clear());
        assertThrows(UnsupportedOperationException.class, () -> conversion.information().put(key, "new_value"));
        assertEquals("value", conversion.information().get(key));

        var generation = TestUtils.generationContext(Map.of(key, "value"));

        assertThrows(UnsupportedOperationException.class, () -> generation.information().clear());
        assertThrows(UnsupportedOperationException.class, () -> generation.information().put(key, "new_value"));
        assertEquals("value", generation.information().get(key));
    }

    @Test
    public void testDefaultImplementations() {
        var key1 = TestUtils.key("key1", String.class);
        var key2 = TestUtils.key("key2", String.class);

        var context = TestUtils.context(Map.of(key1, "value"));

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
        var type1 = TestUtils.key("type", String.class);
        var type2 = TestUtils.key("type", Integer.class);

        var context = TestUtils.context(Map.of(type1, "value"));

        assertTrue(context.has(type1));
        assertFalse(context.has(type2));

        assertEquals("value", context.get(type1));
        assertNull(context.get(type2));

        assertEquals("value", context.assure(type1));
        assertThrows(NoSuchElementException.class, () -> context.assure(type2));

    }

    @Test
    public void testDifferentTypeEquality() {
        var type1 = TestUtils.key("type", String.class);
        var type2 = TestUtils.key("type", Integer.class);

        assertEquals(type1, type2);

        var map = new HashMap<LootContext.Key<?>, Object>();
        map.put(type1, "value");
        map.put(type2, 2);

        assertEquals(Map.of(type2, 2), map);
    }

    @Test
    public void testAllowingSubtypes() {
        class SuperType {}
        class SubType extends SuperType {}

        var superTypeKey = TestUtils.key("type", SuperType.class);
        var subTypeKey = TestUtils.key("type", SubType.class);

        var context = TestUtils.context(Map.of(superTypeKey, new SubType()));
        assertNotNull(context.get(superTypeKey));

        var context2 = TestUtils.context(Map.of(superTypeKey, new SuperType()));
        assertNull(context2.get(subTypeKey));
    }

}
