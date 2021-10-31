package dev.goldenstack.loot.util;

import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.item.attribute.AttributeSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Utility class with the goal of making it easier and faster to get values of an instance by their key.
 */
public class EnumValues <T extends Enum<?>> {

    /**
     * Static constant for the enum {@link AttributeSlot}
     */
    public static final @NotNull EnumValues<AttributeSlot> ATTRIBUTE_SLOT = new EnumValues<>(AttributeSlot::values);
    /**
     * Static constant for the enum {@link AttributeOperation}
     */
    public static final @NotNull EnumValues<AttributeOperation> ATTRIBUTE_OPERATION = new EnumValues<>(AttributeOperation::values);

    private Map<String, T> values = null;
    private final Supplier<T[]> supplier;
    private final boolean concurrent;

    /**
     * Creates a new EnumValues instance that is concurrent with the provided supplier.
     */
    public EnumValues(@NotNull Supplier<T[]> supplier){
        this(supplier, true);
    }

    /**
     * Creates a new EnumValues instance with the provided supplier and whether or not it will use a ConcurrentHashMap
     * instead of a normal HashMap.
     */
    public EnumValues(@NotNull Supplier<T[]> supplier, boolean concurrent){
        this.supplier = supplier;
        this.concurrent = concurrent;
    }

    /**
     * Gets the value of the provided key from the map. Instead of throwing an exception like {@link Enum#valueOf(Class, String)},
     * it returns null of there is no result.
     * @param key The key to check
     * @return The instance of {@code T} that was found, or null if none.
     */
    public @Nullable T valueOf(@NotNull String key){
        if (values == null){
            initializeValues();
        }
        return values.get(key);
    }

    /**
     * Initializes this instance's map. No checks are done to assure that the map has not already been initialized.
     */
    public void initializeValues(){
        T[] array = this.supplier.get();
        this.values = this.concurrent ?
                new ConcurrentHashMap<>((int)(array.length / 0.75) + 1) :
                new HashMap<>((int)(array.length / 0.75) + 1);
        for (T t : array){
            values.put(t.name(), t);
        }
    }
}
