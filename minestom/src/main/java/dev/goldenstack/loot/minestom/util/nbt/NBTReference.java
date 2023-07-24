package dev.goldenstack.loot.minestom.util.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Refers to some NBT instance. This is meant as a way to implement to achieve deep mutability of NBT.
 */
public sealed interface NBTReference permits NBTReferenceImpl {

    /**
     * Creates a new reference to the provided NBT. Currently, this uses an {@link AtomicReference} with its get and set
     * methods.
     * @param nbt the nbt to wrap
     * @return a reference to the provided NBT
     */
    static @NotNull NBTReference of(@NotNull NBT nbt) {
        AtomicReference<NBT> reference = new AtomicReference<>(nbt);
        return new NBTReferenceImpl(reference::get, reference::set);
    }

    /**
     * Gets the value of this NBT reference.
     * @return the current value of this reference
     */
    NBT get();

    /**
     * Sets the value of this NBT reference.
     * @param nbt the new value of this reference
     */
    void set(NBT nbt);

    /**
     * Returns the size of this element if it is a list, or -1 if this is not a list.
     * @return the size of this list, or -1
     */
    default int listSize() {
        return get() instanceof NBTList<?> list ? list.getSize() : -1;
    }

    /**
     * Returns a stream containing a reference to each child of this list, or an empty stream if it is not a list.
     * @return a stream representing this element
     */
    default @NotNull Stream<NBTReference> asStream() {
        return get() instanceof NBTList<?> list ?
                IntStream.range(0, list.getSize()).mapToObj(this::get) : Stream.empty();
    }

    /**
     * Adds the provided value to this element if this is a list of the same type as the element
     * @param value the value to try to add
     */
    default void tryListAdd(@NotNull NBT value) {
        if (get() instanceof NBTList<?> list && (list.isEmpty() || list.getSubtagType() == value.getID())) {
            List<NBT> newList = new ArrayList<>(list.asListView());
            newList.add(value);
            set(new NBTList<>(list.getSubtagType(), newList));
        }
    }

    /**
     * Checks whether or not this element has a value under the provided key
     * @param key the key to check
     * @return true if the element is a NBT compound and the compound contains the provided key
     */
    default boolean has(@NotNull String key) {
        return get() instanceof NBTCompound compound && compound.containsKey(key);
    }

    /**
     * Creates a reference that refers to the value under the provided key of the element that this reference refers to,
     * as long as the element is a NBT reference.
     * @param key the key to get a reference of
     * @return a new reference that refers to the provided key on this reference's NBT
     */
    default NBTReference get(@NotNull String key) {
        return new NBTReferenceImpl(() -> tryCompoundGet(key), nbt -> tryCompoundSet(key, nbt));
    }

    /**
     * Creates a reference that refers to the value under the provided index of the element that this reference refers
     * to, as long as the element is a NBT list.
     * @param index the index to get a reference of
     * @return a new reference that refers to the provided index on this reference's NBT
     */
    default NBTReference get(int index) {
        return new NBTReferenceImpl(() -> tryListGet(index), nbt -> tryListSet(index, nbt));
    }

    private @Nullable NBT tryCompoundGet(@NotNull String key) {
        return get() instanceof NBTCompound compound ? compound.get(key) : null;
    }

    private void tryCompoundSet(@NotNull String key, @NotNull NBT value) {
        if (get() instanceof NBTCompound compound) {
            set(compound.toMutableCompound().set(key, value).toCompound());
        }
    }

    private @Nullable NBT tryListGet(int index) {
        if (get() instanceof NBTList<?> list && index >= 0 && index < list.getSize()) {
            return list.get(index);
        }
        return null;
    }

    private void tryListSet(int index, @NotNull NBT value) {
        if (get() instanceof NBTList<?> list && list.getSubtagType() == value.getID()
                && index >= 0 && index < list.getSize()) {
            List<NBT> newList = new ArrayList<>(list.asListView());
            newList.set(index, value);
            set(new NBTList<>(list.getSubtagType(), newList));
        }
    }

}

record NBTReferenceImpl(@NotNull Supplier<NBT> getter, @NotNull Consumer<NBT> setter) implements NBTReference {

    @Override
    public NBT get() {
        return getter.get();
    }

    @Override
    public void set(NBT nbt) {
        setter.accept(nbt);
    }
}