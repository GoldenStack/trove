package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class Template {

    public static <T> @NotNull BinaryTagSerializer<T> todo(@NotNull String message) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                throw new UnsupportedOperationException("TODO: " + message);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                throw new UnsupportedOperationException("TODO: " + message);
            }
        };
    }

    public static <T> @NotNull BinaryTagSerializer<T> compoundSplit(@NotNull BinaryTagSerializer<? extends T> inline, @NotNull BinaryTagSerializer<T> compound) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                return compound.write(context, value);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                return (tag instanceof CompoundBinaryTag ? compound : inline).read(context, tag);
            }
        };
    }

    @SafeVarargs
    public static <T> @NotNull BinaryTagSerializer<T> constant(@NotNull Function<T, String> name, @NotNull T @NotNull ... entries) {
        Map<String, T> named = Arrays.stream(entries).collect(Collectors.toMap(name, Function.identity()));

        return BinaryTagSerializer.STRING.map(named::get, name);
    }

    public record Entry<T>(@NotNull String key, @NotNull Class<T> type, @NotNull BinaryTagSerializer<T> serializer) {}

    public static <T> @NotNull Entry<T> entry(@NotNull String key, @NotNull Class<T> type, @NotNull BinaryTagSerializer<T> serializer) {
        return new Entry<>(key, type, serializer);
    }

    @SafeVarargs
    public static <T> @NotNull BinaryTagSerializer<T> registry(@NotNull String key, @NotNull Entry<? extends T> @NotNull ... entries) {
        Map<String, Entry<? extends T>> named = Arrays.stream(entries).collect(Collectors.toMap(Entry::key, Function.identity()));
        Map<Class<? extends T>, Entry<? extends T>> typed = Arrays.stream(entries).collect(Collectors.toMap(Entry::type, Function.identity()));

        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                return handle(typed.get(value.getClass()), context, value);
            }

            @SuppressWarnings("unchecked")
            private <N extends T> BinaryTag handle(@NotNull Entry<N> entry, Context context, T value) {
                return entry.serializer().write(context, (N) value);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");
                if (!(tag.get(key) instanceof StringBinaryTag string)) throw new IllegalArgumentException("Expected a string at key '" + key + "'");

                return named.get(string.value()).serializer().read(context, tag);
            }
        };
    }

    @FunctionalInterface
    public interface F1<P1, R> {
        R apply(P1 p1);
    }

    @FunctionalInterface
    public interface F2<P1, P2, R> {
        R apply(P1 p1, P2 p2);
    }

    @FunctionalInterface
    public interface F3<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    @FunctionalInterface
    public interface F4<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    @FunctionalInterface
    public interface F5<P1, P2, P3, P4, P5, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    public static <R> BinaryTagSerializer<R> template(Supplier<R> supplier) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.empty();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                return supplier.get();
            }
        };
    }

    public static <P1, R> BinaryTagSerializer<R> template(
            @NotNull String p1, @NotNull BinaryTagSerializer<P1> s1, @NotNull Function<R, P1> g1,
            @NotNull F1<P1, R> constructor) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.builder()
                        .put(p1, s1.write(context, g1.apply(value)))
                        .build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                return constructor.apply(
                        s1.read(context, tag.get(p1))
                );
            }
        };
    }

    public static <P1, P2, R> BinaryTagSerializer<R> template(
            @NotNull String p1, @NotNull BinaryTagSerializer<P1> s1, @NotNull Function<R, P1> g1,
            @NotNull String p2, @NotNull BinaryTagSerializer<P2> s2, @NotNull Function<R, P2> g2,
            @NotNull F2<P1, P2, R> constructor) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.builder()
                        .put(p1, s1.write(context, g1.apply(value)))
                        .put(p2, s2.write(context, g2.apply(value)))
                        .build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                return constructor.apply(
                        s1.read(context, tag.get(p1)),
                        s2.read(context, tag.get(p2))
                );
            }
        };
    }

    public static <P1, P2, P3, R> BinaryTagSerializer<R> template(
            @NotNull String p1, @NotNull BinaryTagSerializer<P1> s1, @NotNull Function<R, P1> g1,
            @NotNull String p2, @NotNull BinaryTagSerializer<P2> s2, @NotNull Function<R, P2> g2,
            @NotNull String p3, @NotNull BinaryTagSerializer<P3> s3, @NotNull Function<R, P3> g3,
            @NotNull F3<P1, P2, P3, R> constructor) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.builder()
                        .put(p1, s1.write(context, g1.apply(value)))
                        .put(p2, s2.write(context, g2.apply(value)))
                        .put(p3, s3.write(context, g3.apply(value)))
                        .build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                return constructor.apply(
                        s1.read(context, tag.get(p1)),
                        s2.read(context, tag.get(p2)),
                        s3.read(context, tag.get(p3))
                );
            }
        };
    }

    public static <P1, P2, P3, P4, R> BinaryTagSerializer<R> template(
            @NotNull String p1, @NotNull BinaryTagSerializer<P1> s1, @NotNull Function<R, P1> g1,
            @NotNull String p2, @NotNull BinaryTagSerializer<P2> s2, @NotNull Function<R, P2> g2,
            @NotNull String p3, @NotNull BinaryTagSerializer<P3> s3, @NotNull Function<R, P3> g3,
            @NotNull String p4, @NotNull BinaryTagSerializer<P4> s4, @NotNull Function<R, P4> g4,
            @NotNull F4<P1, P2, P3, P4, R> constructor) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.builder()
                        .put(p1, s1.write(context, g1.apply(value)))
                        .put(p2, s2.write(context, g2.apply(value)))
                        .put(p3, s3.write(context, g3.apply(value)))
                        .put(p4, s4.write(context, g4.apply(value)))
                        .build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                return constructor.apply(
                        s1.read(context, tag.get(p1)),
                        s2.read(context, tag.get(p2)),
                        s3.read(context, tag.get(p3)),
                        s4.read(context, tag.get(p4))
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, R> BinaryTagSerializer<R> template(
            @NotNull String p1, @NotNull BinaryTagSerializer<P1> s1, @NotNull Function<R, P1> g1,
            @NotNull String p2, @NotNull BinaryTagSerializer<P2> s2, @NotNull Function<R, P2> g2,
            @NotNull String p3, @NotNull BinaryTagSerializer<P3> s3, @NotNull Function<R, P3> g3,
            @NotNull String p4, @NotNull BinaryTagSerializer<P4> s4, @NotNull Function<R, P4> g4,
            @NotNull String p5, @NotNull BinaryTagSerializer<P5> s5, @NotNull Function<R, P5> g5,
            @NotNull F5<P1, P2, P3, P4, P5, R> constructor) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.builder()
                        .put(p1, s1.write(context, g1.apply(value)))
                        .put(p2, s2.write(context, g2.apply(value)))
                        .put(p3, s3.write(context, g3.apply(value)))
                        .put(p4, s4.write(context, g4.apply(value)))
                        .put(p5, s5.write(context, g5.apply(value)))
                        .build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                return constructor.apply(
                        s1.read(context, tag.get(p1)),
                        s2.read(context, tag.get(p2)),
                        s3.read(context, tag.get(p3)),
                        s4.read(context, tag.get(p4)),
                        s5.read(context, tag.get(p5))
                );
            }
        };
    }

}
