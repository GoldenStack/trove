package net.goldenstack.loot.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

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

    public static final @NotNull BinaryTagSerializer<NamespaceID> KEY = BinaryTagSerializer.STRING.map(NamespaceID::from, NamespaceID::asString);

    public static <T> @NotNull BinaryTagSerializer<DynamicRegistry.Key<T>> key() {
        return KEY.map(DynamicRegistry.Key::of, DynamicRegistry.Key::namespace);
    }

}
