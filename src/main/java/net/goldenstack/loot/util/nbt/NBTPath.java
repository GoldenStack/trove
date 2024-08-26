package net.goldenstack.loot.util.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A NBTPath allows selecting specific elements from a NBT tree, based on a list of selectors. Each selector has the
 * ability to select any number of elements from its predecessor—this allows arbitrary item selection from any NBT type.
 * <br>
 * It also provides multiple ways to manipulate NBT results as there is no deeply mutable NBT implementation.
 */
public record NBTPath(@NotNull List<Selector> selectors) {

    /**
     * Selects an arbitrary number of elements from provided NBT.
     */
    public sealed interface Selector {

        /**
         * Passes each selected NBT element into the given consumer.
         */
        void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer);

        /**
         * Modifies the provided {@code source} so that, if possible, this path selector will select at least one NBT
         * element from it. If a placeholder element is needed, {@code nextElement} is to be used.
         */
        void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement);

        /**
         * Provides a tag that this selector may be willing to modify.
         */
        @NotNull BinaryTag preparedNBT();

        record RootKey(@NotNull String key) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                if (source.has(key)) {
                    consumer.accept(source.get(key));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {}

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return CompoundBinaryTag.empty();
            }

            @Override
            public String toString() {
                return key;
            }
        }

        record Key(@NotNull String key) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                if (source.has(key)) {
                    consumer.accept(source.get(key));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                if (!source.has(key)) {
                    source.get(key).set(nextElement.get());
                }
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return CompoundBinaryTag.empty();
            }

            @Override
            public String toString() {
                return "." + key;
            }
        }

        record CompoundFilter(@NotNull CompoundBinaryTag filter) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                if (NBTUtils.compareNBT(filter, source.get(), false)) {
                    consumer.accept(source);
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                if (!NBTUtils.compareNBT(filter, source.get(), false)) {
                    source.set(filter);
                }
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return CompoundBinaryTag.empty();
            }

            @Override
            public String toString() {
                try {
                    return TagStringIO.get().asString(filter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record EntireList() implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                int size = source.listSize();
                for (int i = 0; i < size; i++) {
                    consumer.accept(source.get(i));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                if (source.listSize() == 0) {
                    source.listAdd(nextElement.get());
                }
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return ListBinaryTag.empty();
            }

            @Override
            public String toString() {
                return "[]";
            }
        }

        record Index(int index) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                var newIndex = index >= 0 ? index : source.listSize() + index;

                if (newIndex < 0 || newIndex >= source.listSize()) return;

                consumer.accept(source.get(newIndex));
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {}

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return ListBinaryTag.empty();
            }

            @Override
            public String toString() {
                return "[" + index + "]";
            }
        }

        record ListFilter(@NotNull CompoundBinaryTag filter) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                int listSize = source.listSize();
                for (int i = 0; i < listSize; i++) {
                    NBTReference ref = source.get(i);
                    if (NBTUtils.compareNBT(filter, ref.get(), false)) {
                        consumer.accept(ref);
                    }
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                int listSize = source.listSize();
                if (listSize == -1) return;

                for (int i = 0; i < listSize; i++) {
                    if (NBTUtils.compareNBT(filter, source.get(i).get(), false)) {
                        return;
                    }
                }

                source.listAdd(filter);
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return ListBinaryTag.empty();
            }

            @Override
            public String toString() {
                try {
                    return "[" + TagStringIO.get().asString(filter) + "]";
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * Strings {@code source} through each selector in {@link #selectors()}, returning the selected results. It is
     * possible for there to be none. Modifying the resulting NBT references does nothing.
     * @param source the source, to get the NBT from
     * @return the list of selected NBT, which may be empty
     */
    public @NotNull List<NBTReference> get(@NotNull BinaryTag source) {
        List<NBTReference> references = List.of(NBTReference.of(source));
        for (var selector : selectors()) {
            List<NBTReference> newReferences = new ArrayList<>();

            references.forEach(nbt -> selector.get(nbt, newReferences::add));

            if (newReferences.isEmpty()) {
                return List.of();
            }
            references = newReferences;
        }
        return references;
    }

    /**
     * Strings {@code source} through each selector, making each selector
     * {@link Selector#prepare(NBTReference, Supplier) prepare} each element. This should result in this method
     * returning nothing much less often, although it is still possible. Modifying the resulting NBT references will
     * result in the provided {@code source} being modified.
     * @param source the source, to get the NBT from
     * @param finalDefault the default value for results produced from the last path selector
     * @return the list of selected NBT, which may be empty
     */
    public @NotNull List<NBTReference> getWithDefaults(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> finalDefault) {
        List<NBTReference> references = List.of(source);

        for (int selectorIndex = 0; selectorIndex < selectors().size(); selectorIndex++) {
            var selector = selectors().get(selectorIndex);
            Supplier<BinaryTag> next = (selectorIndex < selectors().size() - 1) ? selectors().get(selectorIndex + 1)::preparedNBT : finalDefault;

            List<NBTReference> newNBT = new ArrayList<>();
            for (var nbt : references) {
                selector.prepare(nbt, next);
                selector.get(nbt, newNBT::add);
            }
            if (newNBT.isEmpty()) {
                return List.of();
            }

            references = newNBT;
        }

        return references;
    }

    /**
     * Strings {@code source} through each selector, making each selector
     * {@link Selector#prepare(NBTReference, Supplier) prepare} each element. This should result in this method
     * returning nothing much less often, although it is still possible. Modifying the resulting NBT references will
     * result in the provided {@code source} being modified. This is equivalent to calling
     * {@link #getWithDefaults(NBTReference, Supplier)} and setting all of the results to {@code setValue}.
     * @param source the source, to get the NBT from
     * @param setValue the value to set all selected elements to
     * @return the list of selected NBT, which may be empty
     */
    public @NotNull List<NBTReference> set(@NotNull NBTReference source, @NotNull BinaryTag setValue) {
        List<NBTReference> references = getWithDefaults(source, () -> setValue);
        for (var reference : references) {
            reference.set(setValue);
        }
        return references;
    }

}

