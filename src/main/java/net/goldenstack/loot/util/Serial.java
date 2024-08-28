package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class Serial {

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

    public static final @NotNull BinaryTagSerializer<NamespaceID> KEY = BinaryTagSerializer.STRING.map(NamespaceID::from, NamespaceID::asString);

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

}
