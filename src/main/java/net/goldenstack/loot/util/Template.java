package net.goldenstack.loot.util;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class Template {

    // TODO: Need equivalent of compoundSplit (investigate orElse usage)

    private static final @NotNull Map<Key, DataComponent<List<ItemStack>>> NAMED_CONTAINERS =
            Stream.of(DataComponents.CONTAINER, DataComponents.BUNDLE_CONTENTS, DataComponents.CHARGED_PROJECTILES)
                    .collect(Collectors.toMap(DataComponent::key, Function.identity()));

    public static final @NotNull Codec<DataComponent<List<ItemStack>>> CONTAINER = Codec.KEY.transform(NAMED_CONTAINERS::get, DataComponent::key);

    public static <T> @NotNull Codec<List<DynamicRegistry.Key<T>>> keyOrTag(@NotNull Registries.Selector<T> selector, @NotNull Tag.BasicType type) {
        return Codec.RegistryKey(selector).list()
                .orElse(Codec.STRING.transform(string -> {
                    if (string.startsWith("#")) {
                        List<DynamicRegistry.Key<T>> values = new ArrayList<>();
                        MinecraftServer.getTagManager()
                                .getTag(type, string.substring(1))
                                .getValues()
                                .forEach(value -> values.add(DynamicRegistry.Key.of(value)));
                        return values;
                    } else {
                        return List.of(DynamicRegistry.Key.of(string));
                    }
                }, ignored -> {
                    throw new UnsupportedOperationException();
                }));
    }

    @SafeVarargs
    public static <T> @NotNull Codec<T> constant(@NotNull Function<T, String> name, @NotNull T @NotNull ... entries) {
        return constant(name, Arrays.asList(entries));
    }

    public static <T> @NotNull Codec<T> constant(@NotNull Function<T, String> name, @NotNull Collection<T> entries) {
        Map<String, T> named = entries.stream().collect(Collectors.toMap(name, Function.identity()));

        return Codec.STRING.transform(string -> Objects.requireNonNull(named.get(string)), name::apply);
    }

//    public static <T> @NotNull BinaryTagSerializer<T> compoundSplit(@NotNull BinaryTagSerializer<? extends T> inline, @NotNull BinaryTagSerializer<T> compound) {
//        return new BinaryTagSerializer<>() {
//            @Override
//            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
//                return compound.write(context, value);
//            }
//
//            @Override
//            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
//                return (tag instanceof CompoundBinaryTag ? compound : inline).read(context, tag);
//            }
//        };
//    }
//
//    public record Entry<T>(@NotNull String key, @NotNull Class<T> type, @NotNull BinaryTagSerializer<T> serializer) {}
//
//    public static <T> @NotNull Entry<T> entry(@NotNull String key, @NotNull Class<T> type, @NotNull BinaryTagSerializer<T> serializer) {
//        key = NamespaceID.from(key).asString();
//        return new Entry<>(key, type, serializer);
//    }
//
//    @SafeVarargs
//    public static <T> @NotNull BinaryTagSerializer<T> registry(@NotNull String key, @NotNull Entry<? extends T> @NotNull ... entries) {
//        Map<String, Entry<? extends T>> named = Arrays.stream(entries).collect(Collectors.toMap(Entry::key, Function.identity()));
//        Map<Class<? extends T>, Entry<? extends T>> typed = Arrays.stream(entries).collect(Collectors.toMap(Entry::type, Function.identity()));
//
//        return new BinaryTagSerializer<>() {
//            @Override
//            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
//                return handle(typed.get(value.getClass()), context, value);
//            }
//
//            @SuppressWarnings("unchecked")
//            private <N extends T> BinaryTag handle(@NotNull Entry<N> entry, Context context, T value) {
//                BinaryTag tag = entry.serializer().write(context, (N) value);
//                if (tag instanceof CompoundBinaryTag compoud) {
//                    return compoud.put(key, StringBinaryTag.stringBinaryTag(entry.key()));
//                } else {
//                    return tag;
//                }
//            }
//
//            @Override
//            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag raw) {
//                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");
//                if (!(tag.get(key) instanceof StringBinaryTag string)) throw new IllegalArgumentException("Expected a string at key '" + key + "'");
//
//                Entry<? extends T> entry = named.get(string.value());
//                if (entry == null) throw new IllegalArgumentException("Invalid named key '" + string.value() + "'");
//
//                return entry.serializer().read(context, tag);
//            }
//        };
//    }

}
