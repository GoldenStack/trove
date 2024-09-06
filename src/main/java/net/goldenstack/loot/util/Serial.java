package net.goldenstack.loot.util;

import net.goldenstack.loot.LootPredicate;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class Serial {

    // Cached components
    public static final @NotNull BinaryTagSerializer<NamespaceID> KEY = BinaryTagSerializer.STRING.map(NamespaceID::from, NamespaceID::asString);
    public static final @NotNull BinaryTagSerializer<List<LootPredicate>> PREDICATES = Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of());
    public static final @NotNull BinaryTagSerializer<List<Component>> COMPONENTS = BinaryTagSerializer.NBT_COMPONENT.list();
    public static final @NotNull BinaryTagSerializer<List<FilteredText<String>>> STRING_PAGES = FilteredText.STRING_NBT_TYPE.list();
    public static final @NotNull BinaryTagSerializer<List<FilteredText<Component>>> COMPONENT_PAGES = FilteredText.COMPONENT_NBT_TYPE.list();
    public static final @NotNull BinaryTagSerializer<@Nullable RelevantEntity> OPTIONAL_ENTITY = RelevantEntity.SERIALIZER.optional();
    public static final @NotNull BinaryTagSerializer<List<FireworkExplosion>> EXPLOSIONS = FireworkExplosion.NBT_TYPE.list();
    public static final @NotNull BinaryTagSerializer<@Nullable Integer> OPTIONAL_INT = BinaryTagSerializer.INT.optional();

    private static final @NotNull Map<NamespaceID, DataComponent<List<ItemStack>>> NAMED_CONTAINERS =
            Stream.of(ItemComponent.CONTAINER, ItemComponent.BUNDLE_CONTENTS, ItemComponent.CHARGED_PROJECTILES)
                    .collect(Collectors.toMap(DataComponent::namespace, Function.identity()));

    public static final @NotNull BinaryTagSerializer<DataComponent<List<ItemStack>>> CONTAINER = KEY.map(NAMED_CONTAINERS::get, DataComponent::namespace);

    public static final @NotNull BinaryTagSerializer<Double> DOUBLE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull Double value) {
            return DoubleBinaryTag.doubleBinaryTag(value);
        }

        @Override
        public @NotNull Double read(@NotNull Context context, @NotNull BinaryTag raw) {
            if (!(raw instanceof NumberBinaryTag number)) throw new IllegalArgumentException("Expected a number tag");
            return number.doubleValue();
        }
    };

    public static final @NotNull BinaryTagSerializer<Long> LONG = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull Long value) {
            return LongBinaryTag.longBinaryTag(value);
        }

        @Override
        public @NotNull Long read(@NotNull Context context, @NotNull BinaryTag raw) {
            if (!(raw instanceof NumberBinaryTag number)) throw new IllegalArgumentException("Expected a number tag");
            return number.longValue();
        }
    };

    public static @NotNull BinaryTagSerializer<Tag> tag(@NotNull Tag.BasicType type) {
        return BinaryTagSerializer.STRING.map(str -> MinecraftServer.getTagManager().getTag(type, str), Tag::name);
    }

    public static <T> @NotNull BinaryTagSerializer<DynamicRegistry.Key<T>> key() {
        return KEY.map(DynamicRegistry.Key::of, DynamicRegistry.Key::namespace);
    }

    public static <T> @NotNull BinaryTagSerializer<T> lazy(@NotNull Supplier<BinaryTagSerializer<T>> supplier) {
        return new BinaryTagSerializer<>() {
            private BinaryTagSerializer<T> delegate;

            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                if (delegate == null) delegate = supplier.get();
                return delegate.write(context, value);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (delegate == null) delegate = supplier.get();
                return delegate.read(context, tag);
            }
        };
    }

    public static <V> @NotNull BinaryTagSerializer<Map<String, V>> map(@NotNull BinaryTagSerializer<V> serializer) {
        return map(Function.identity(), Function.identity(), serializer);
    }

    public static <K, V> @NotNull BinaryTagSerializer<Map<K, V>> map(@NotNull Function<String, K> to, @NotNull Function<K, String> from,
                                                                     @NotNull BinaryTagSerializer<V> serializer) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Map<K, V> value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

                for (var entry : value.entrySet()) {
                    builder.put(from.apply(entry.getKey()), serializer.write(context, entry.getValue()));
                }

                return builder.build();
            }

            @Override
            public @NotNull Map<K, V> read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                Map<K, V> map = new HashMap<>();

                for (var entry : tag) {
                    map.put(to.apply(entry.getKey()), serializer.read(context, entry.getValue()));
                }

                return map;
            }
        };
    }

    public static <T> @NotNull BinaryTagSerializer<List<T>> coerceList(@NotNull BinaryTagSerializer<T> serializer) {
        BinaryTagSerializer<List<T>> list = serializer.list();
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull List<T> value) {
                return value.size() == 1 ? serializer.write(value.getFirst()) : list.write(context, value);
            }

            @Override
            public @NotNull List<T> read(@NotNull Context context, @NotNull BinaryTag tag) {
                return tag instanceof ListBinaryTag ? list.read(context, tag) : List.of(serializer.read(context, tag));
            }
        };
    }

}
