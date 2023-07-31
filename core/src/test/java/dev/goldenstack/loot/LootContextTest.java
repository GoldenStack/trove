package dev.goldenstack.loot;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class LootContextTest {

    @Test
    public void testDefaultImplementations() {
        var key1 = TestUtils.key("key1", String.class);
        var key2 = TestUtils.key("key2", String.class);

        var context = TestUtils.context(key1, "value");

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

        var context = TestUtils.context(type1, "value");

        assertTrue(context.has(type1));
        assertFalse(context.has(type2));

        assertEquals("value", context.get(type1));
        assertNull(context.get(type2));

        assertEquals("value", context.assure(type1));
        assertThrows(NoSuchElementException.class, () -> context.assure(type2));

    }

    @Test
    public void testAllowingSubtypes() {
        class SuperType {}
        class SubType extends SuperType {}

        var superTypeKey = TestUtils.key("type", SuperType.class);
        var subTypeKey = TestUtils.key("type", SubType.class);

        var context = TestUtils.context(superTypeKey, new SubType());
        assertNotNull(context.get(superTypeKey));

        var context2 = TestUtils.context(superTypeKey, new SuperType());
        assertNull(context2.get(subTypeKey));
    }

}
