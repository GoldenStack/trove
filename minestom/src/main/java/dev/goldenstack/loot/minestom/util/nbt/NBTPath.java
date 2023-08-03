package dev.goldenstack.loot.minestom.util.nbt;

import dev.goldenstack.loot.converter.TypedLootConverter;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A NBTPath allows specific elements from a NBT tree to be selected, based on a parsed string. The general structure of
 * them involves a list of {@link Selector}s, each selecting a certain number of elements from the previous set of
 * elements, making its way down the tree until the resulting elements can be returned. It also provides multiple ways
 * to manipulate results, via the {@link NBTReference} class, as there is no deeply mutable NBT implementation available.
 */
public sealed interface NBTPath permits NBTPathImpl {

    /**
     * A converter that tries to read a NBT path from a string, and will convert it back to a string, too.
     */
    @NotNull TypeSerializer<NBTPath> CONVERTER = NBTPathImpl.CONVERTER;

    /**
     * Reads a NBT path from the provided reader. It is possible that this does not consume the entire reader, so
     * make sure to check the reader to see if it has been fully read.
     * @param reader the reader to read from
     * @return the read path
     * @throws IOException if there was an exception while trying to read the path
     */
    static @NotNull NBTPath readPath(@NotNull StringReader reader) throws IOException {
        return NBTPathImpl.readPath(reader, null);
    }

    /**
     * Selects an arbitrary number of elements from a provided NBT element.
     */
    interface Selector {

        /**
         * Selects, if possible, the element under the {@link #key()} key of the provided source.<br>
         * When told to prepare, this not add anything to the root.
         * @param key the key to select
         */
        record RootKey(@NotNull String key) implements Selector {

            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements) {
                if (source.has(key)) {
                    selectedElements.accept(source.get(key));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement) {}

            @Override
            public @NotNull NBT preparedNBT() {
                return new NBTCompound();
            }

            @Override
            public String toString() {
                return key;
            }
        }

        /**
         * Selects, if possible, the element under the {@link #key()} key of the provided source.<br>
         * When told to prepare, it will set the key under {@link #key()} of the provided element to the {@code nextElement}
         * if it is not set.
         * @param key the key to select
         */
        record CompoundKey(@NotNull String key) implements Selector {

            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements) {
                if (source.has(key)) {
                    selectedElements.accept(source.get(key));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement) {
                if (!source.has(key)) {
                    source.get(key).set(nextElement.get());
                }
            }

            @Override
            public @NotNull NBT preparedNBT() {
                return new NBTCompound();
            }

            @Contract(pure = true)
            @Override
            public @NotNull String toString() {
                return "." + key;
            }
        }

        /**
         * Selects the provided source if it passes the {@link #filter()}.<br>
         * When told to prepare, it will override the source element with {@link #filter()} if the source element does not
         * pass the filter.
         * @param filter the filter to use
         */
        record CompoundFilter(@NotNull NBTCompound filter) implements Selector {

            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements) {
                if (NBTUtils.compareNBT(filter, source.get(), false)) {
                    selectedElements.accept(source);
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement) {
                if (!NBTUtils.compareNBT(filter, source.get(), false)) {
                    source.set(filter);
                }
            }

            @Override
            public @NotNull NBT preparedNBT() {
                return new NBTCompound();
            }

            @Override
            public @NotNull String toString() {
                return filter.toSNBT();
            }
        }

        /**
         * Selects, if possible, the element of index {@link #index()} from the provided list, or, if the index is negative,
         * selects the nth element from the end of the list, where n is {@link #index()}.<br>
         * When asked to prepare, it will do nothing.
         * @param index the index to select, or negative to select starting from the end
         */
        record ListIndex(int index) implements Selector {

            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements) {
                var newIndex = index >= 0 ? index : source.listSize() + index;

                if (newIndex < 0) return;

                source.asStream().skip(newIndex).findFirst().ifPresent(selectedElements);
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement) {}

            @Override
            public @NotNull NBT preparedNBT() {
                return new NBTList<>(NBTType.TAG_End);
            }

            @Contract(pure = true)
            @Override
            public @NotNull String toString() {
                return "[" + index + "]";
            }
        }

        /**
         * Selects, if possible, each element from the provided list that fits the {@link #filter()}.<br>
         * When asked to prepare, it will, if the provided element is a list, add {@link #filter()} to the list if nothing
         * matches the filter.
         * @param filter the filter for each element in the list
         */
        record ListFilter(@NotNull NBTCompound filter) implements Selector {

            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements) {
                source.asStream().filter(nbt -> NBTUtils.compareNBT(filter, nbt.get(), false)).forEach(selectedElements);
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement) {
                if (source.asStream().noneMatch(nbt -> NBTUtils.compareNBT(filter, nbt.get(), false))) {
                    source.tryListAdd(filter);
                }
            }

            @Override
            public @NotNull NBT preparedNBT() {
                return new NBTList<>(NBTType.TAG_End);
            }

            @Override
            public @NotNull String toString() {
                return "[" + filter.toSNBT() + "]";
            }
        }

        /**
         * Selects, if possible, every item from the provided list.<br>
         * When asked to prepare, it will add the {@code nextElement} to the list if the list is empty.
         */
        record EntireList() implements Selector {

            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements) {
                source.asStream().forEach(selectedElements);
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement) {
                if (source.listSize() == 0) {
                    source.tryListAdd(nextElement.get());
                }
            }

            @Override
            public @NotNull NBT preparedNBT() {
                return new NBTList<>(NBTType.TAG_End);
            }

            @Contract(pure = true)
            @Override
            public @NotNull String toString() {
                return "[]";
            }
        }

        /**
         * Provides each selected NBT element from {@code source} into {@code selectedElements}.
         * @param source the reference that is the source NBT element
         * @param selectedElements the consumer for selected NBT references
         */
        void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> selectedElements);

        /**
         * Modifies the provided {@code source} so that, if possible, this path selector will select at least one NBT
         * element from it. If a placeholder element is needed, {@code nextElement} is to be used. Importantly, it is
         * permitted that this method does not make {@link #get(NBTReference, Consumer)} supply any elements; it is
         * simply giving it the opportunity to do so.
         * @param source the reference that is the source NBT element
         * @param nextElement the supplier for the next element to use
         */
        void prepare(@NotNull NBTReference source, @NotNull Supplier<NBT> nextElement);

        /**
         * Provides a NBT element that this selector is willing to modify in {@link #prepare(NBTReference, Supplier)}.
         * This will likely be passed as the {@code nextElement} parameter of the previous element in the NBT path.
         * @return the new prepared NBT
         */
        @NotNull NBT preparedNBT();

    }

    /**
     * Gets the list of selectors that this path uses.
     * @return this path's selectors
     */
    @NotNull List<Selector> selectors();

    /**
     * Strings {@code source} through each selector in {@link #selectors()}, returning the selected results. It is
     * possible for there to be none. Modifying the resulting NBT references does nothing.
     * @param source the source, to get the NBT from
     * @return the list of selected NBT, which may be empty
     */
    default @NotNull List<NBTReference> get(@NotNull NBT source) {
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
    default @NotNull List<NBTReference> getWithDefaults(@NotNull NBTReference source, @NotNull Supplier<NBT> finalDefault) {
        List<NBTReference> references = List.of(source);

        for (int selectorIndex = 0; selectorIndex < selectors().size(); selectorIndex++) {
            var selector = selectors().get(selectorIndex);
            Supplier<NBT> next = (selectorIndex < selectors().size() - 1) ? selectors().get(selectorIndex + 1)::preparedNBT : finalDefault;

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
    default @NotNull List<NBTReference> set(@NotNull NBTReference source, @NotNull NBT setValue) {
        List<NBTReference> references = getWithDefaults(source, () -> setValue);
        for (var reference : references) {
            reference.set(setValue);
        }
        return references;
    }

}

record NBTPathImpl(@NotNull List<Selector> selectors) implements NBTPath {

    static final @NotNull IntSet VALID_SELECTOR_STARTERS = IntSet.of('.', '{', '[');
    static final @NotNull IntSet VALID_INTEGER_CHARACTERS = IntSet.of('-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    static final @NotNull IntSet INVALID_UNQUOTED_CHARACTERS = IntSet.of(-1, '.', '\'', '\"', '{', '}', '[', ']');

    static final @NotNull TypeSerializer<NBTPath> CONVERTER = TypedLootConverter.join(
            (input, result) -> result.set(input.toString()), input -> {
                var path = input.getString();
                if (path == null) {
                    throw new SerializationException(input, NBTPath.class, "Expected a string to deserialize a path from");
                }

                var reader = new StringReader(path);

                try {
                    var parsedPath = readPath(reader, input);

                    if (reader.read() != -1) { // Make sure we read the entire string
                        throw new ConfigurateException(input, "Reading a path from '" + path + "' did not consume the entire reader");
                    }

                    return parsedPath;
                } catch (IOException e) {
                    if (e instanceof SerializationException configurate) {
                        throw configurate;
                    } else {
                        throw new SerializationException(input, NBTPath.class, "Could not read a NBT path from '" + path + "'", e);
                    }
                }
            });

    static @NotNull NBTPath readPath(@NotNull StringReader reader, @Nullable ConfigurationNode source) throws IOException {
        List<Selector> selectors = new ArrayList<>();

        if (!VALID_SELECTOR_STARTERS.contains(peek(reader))) {
            var key = readString(reader);
            if (key != null) {
                selectors.add(new Selector.RootKey(key));
            }
        }

        while (true) {
            reader.mark(0);

            if (!VALID_SELECTOR_STARTERS.contains(peek(reader))) {
                if (selectors.isEmpty()) {
                    reader.reset();
                    String message = "NBT paths must contain at least one selector (reading from " + reader + ")";
                    throw source != null ? new ConfigurateException(source, message) : new ConfigurateException(message);
                }
                return new NBTPathImpl(List.copyOf(selectors));
            }

            var selector = readPathSelector(reader);
            if (selector == null) {
                reader.reset();
                String message = "Invalid NBT path selector (reading from " + reader + ")";
                throw source != null ? new ConfigurateException(source, message) : new ConfigurateException(message);
            }

            selectors.add(selector);
        }
    }

    // Returning null indicates a failure to read
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static @Nullable NBTPath.Selector readPathSelector(@NotNull StringReader reader) throws IOException {
        var firstChar = peek(reader);
        return switch (firstChar) {
            case '.' -> {
                reader.skip(1); // Skip period

                var string = readString(reader);
                yield string != null ? new Selector.CompoundKey(string) : null;
            }
            case '{' -> {
                var compound = NBTUtils.readCompoundSNBT(reader);
                yield compound != null ? new Selector.CompoundFilter(compound) : null;
            }
            case '[' -> {
                reader.skip(1); // Skip opening square brackets

                var secondChar = peek(reader);
                var selector = switch(secondChar) {
                    case ']' -> new Selector.EntireList();
                    case '{' -> {
                        var compound = NBTUtils.readCompoundSNBT(reader);
                        yield compound != null ? new Selector.ListFilter(compound) : null;
                    }
                    default -> {
                        if (VALID_INTEGER_CHARACTERS.contains(secondChar)) {
                            var index = readInteger(reader);
                            yield index != null ? new Selector.ListIndex(index) : null;
                        }
                        yield null;
                    }
                };

                reader.skip(1); // Skip closing square brackets
                yield selector;
            }
            default -> null;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static @Nullable Integer readInteger(@NotNull StringReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();

        int peek;
        while (VALID_INTEGER_CHARACTERS.contains(peek = reader.read())) {
            builder.appendCodePoint(peek);
        }

        // Unread the one extra character that was read; this does nothing if the entire string has been read
        reader.skip(-1);

        try {
            return Integer.parseInt(builder.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static @Nullable String readString(@NotNull StringReader reader) throws IOException {
        var peek = peek(reader);
        if (peek == -1) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        if (peek == '"' || peek == '\'') { // Read quoted string
            reader.skip(1); // Skip the character we know already

            boolean escape = false;

            while (true) {
                var next = reader.read();

                if (next == '\\') { // Read escape character
                    escape = true;
                } else if (next == peek && !escape) { // Return if unescaped closing character
                    return builder.toString();
                } else {
                    if (escape) { // If there was an unused escape, re-add it; there's only one character it's used for
                        builder.appendCodePoint('\\');
                    }

                    if (next == -1) {
                        return null;
                    }

                    builder.appendCodePoint(next); // Add the next character always
                }
            }
        }

        // Read unquoted string
        int read;
        while (!INVALID_UNQUOTED_CHARACTERS.contains(read = reader.read())) {
            builder.appendCodePoint(read);
        }

        // Unread the one extra character that was read; this does nothing if the entire string has been read
        reader.skip(-1);

        return builder.isEmpty() ? null : builder.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static int peek(@NotNull StringReader reader) throws IOException {
        var codePoint = reader.read();
        reader.skip(-1);
        return codePoint;
    }

    @Override
    public String toString() {
        return selectors().stream().map(Selector::toString).collect(Collectors.joining());
    }

}