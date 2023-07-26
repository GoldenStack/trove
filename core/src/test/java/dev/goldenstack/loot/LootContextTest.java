package dev.goldenstack.loot;

import dev.goldenstack.loot.context.LootContext;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class LootContextTest {

    @Test
    public void testDefaultImplementations() {
        var key1 = TestUtils.key("key1", String.class);
        var key2 = TestUtils.key("key2", String.class);

        var context = LootContext.builder().with(key1, "value").build();

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

        var context = LootContext.builder().with(type1, "value").build();

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

        var context = LootContext.builder().with(superTypeKey, new SubType()).build();
        assertNotNull(context.get(superTypeKey));

        var context2 = LootContext.builder().with(superTypeKey, new SuperType()).build();
        assertNull(context2.get(subTypeKey));
    }

}
