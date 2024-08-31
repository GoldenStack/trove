package net.goldenstack.loot.util;

import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public sealed interface ListOperation {

    @SuppressWarnings("UnstableApiUsage")
    @NotNull BinaryTagSerializer<ListOperation> SERIALIZER = Template.registry("mode",
            Template.entry("append", Append.class, Template.template(Append::new)),
            Template.entry("insert", Insert.class, Template.template(
                    "offset", BinaryTagSerializer.INT.optional(0), Insert::offset,
                    Insert::new
            )),
            Template.entry("replace_all", ReplaceAll.class, Template.template(ReplaceAll::new)),
            Template.entry("replace_section", ReplaceSection.class, Template.template(
                    "offset", BinaryTagSerializer.INT.optional(0), ReplaceSection::offset,
                    "size", BinaryTagSerializer.INT.optional(), ReplaceSection::size,
                    ReplaceSection::new
            ))
    );

    <T> @NotNull List<T> apply(@NotNull List<T> values, @NotNull List<T> input);

    record Append() implements ListOperation {
        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            return Stream.concat(input.stream(), values.stream()).toList();

        }
    }

    record Insert(int offset) implements ListOperation {
        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            List<T> items = new ArrayList<>();
            items.addAll(input.subList(0, this.offset));
            items.addAll(values);
            items.addAll(input.subList(this.offset, input.size()));
            return items;
        }
    }

    record ReplaceAll() implements ListOperation {
        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            return values;
        }
    }

    record ReplaceSection(int offset, @Nullable Integer size) implements ListOperation {
        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            List<T> items = new ArrayList<>();
            items.addAll(input.subList(0, offset));
            items.addAll(values);

            int size = this.size != null ? this.size : values.size();

            // Add truncated part of list of possible
            if (offset + size < input.size()) {
                items.addAll(input.subList(offset + size, input.size()));
            }

            return items;
        }
    }

}
