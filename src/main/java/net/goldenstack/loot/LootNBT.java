package net.goldenstack.loot;

import net.goldenstack.loot.util.RelevantEntity;
import net.goldenstack.loot.util.Template;
import net.goldenstack.loot.util.VanillaInterface;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Returns NBT data from the provided context.
 */
public interface LootNBT {

    @NotNull BinaryTagSerializer<LootNBT> SERIALIZER = Template.template(() -> null);

    /**
     * Generates some NBT based on the provided context.
     * @param context the context to use for NBT
     * @return the NBT, or null if there is none
     */
    @Nullable BinaryTag getNBT(@NotNull LootContext context);

    record CommandStorage(@NotNull NamespaceID key) implements LootNBT {
        @Override
        public @Nullable BinaryTag getNBT(@NotNull LootContext context) {
            return context.require(LootContext.COMMAND_STORAGE).apply(key);
        }
    }

    record Context(@NotNull Target target) implements LootNBT {
        public sealed interface Target {
            @Nullable BinaryTag getNBT(@NotNull LootContext context);

            record BlockEntity() implements Target {
                @Override
                public @NotNull BinaryTag getNBT(@NotNull LootContext context) {
                    Block block = context.require(LootContext.BLOCK_STATE);
                    Point pos = context.require(LootContext.ORIGIN);

                    CompoundBinaryTag nbt = block.hasNbt() ? block.nbt() : CompoundBinaryTag.empty();

                    return nbt.put(Map.of(
                            "x", IntBinaryTag.intBinaryTag(pos.blockX()),
                            "y", IntBinaryTag.intBinaryTag(pos.blockY()),
                            "z", IntBinaryTag.intBinaryTag(pos.blockZ()),
                            "id", StringBinaryTag.stringBinaryTag(block.namespace().asString())
                    ));
                }
            }

            record Entity(@NotNull RelevantEntity target) implements Target {
                @Override
                public @NotNull BinaryTag getNBT(@NotNull LootContext context) {
                    var entity = context.require(target.key());
                    VanillaInterface vanilla = context.require(LootContext.VANILLA_INTERFACE);

                    return vanilla.serializeEntity(entity);
                }
            }
        }

        @Override
        public @Nullable BinaryTag getNBT(@NotNull LootContext context) {
            return target.getNBT(context);
        }
    }

}
