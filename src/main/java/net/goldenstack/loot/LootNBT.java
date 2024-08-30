package net.goldenstack.loot;

import net.goldenstack.loot.util.RelevantEntity;
import net.goldenstack.loot.util.Serial;
import net.goldenstack.loot.util.Template;
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
@SuppressWarnings("UnstableApiUsage")
public interface LootNBT {

    @NotNull BinaryTagSerializer<LootNBT> SERIALIZER = Template.compoundSplit(
            BinaryTagSerializer.STRING.map(Context.Target::fromString, Context.Target::toString).map(Context::new, Context::target),
            Template.registry("type",
                    Template.entry("context", Context.class, Context.SERIALIZER),
                    Template.entry("storage", Storage.class, Storage.SERIALIZER)
            )
    );

    /**
     * Generates some NBT based on the provided context.
     * @param context the context to use for NBT
     * @return the NBT, or null if there is none
     */
    @Nullable BinaryTag getNBT(@NotNull LootContext context);

    record Storage(@NotNull NamespaceID source) implements LootNBT {

        public static final @NotNull BinaryTagSerializer<Storage> SERIALIZER = Template.template(
                "source", Serial.KEY, Storage::source,
                Storage::new
        );

        @Override
        public @Nullable BinaryTag getNBT(@NotNull LootContext context) {
            return context.vanilla().commandStorage(source);
        }
    }

    record Context(@NotNull Target target) implements LootNBT {

        public static final @NotNull BinaryTagSerializer<Context> SERIALIZER = Template.template(
                "target", BinaryTagSerializer.STRING.map(Context.Target::fromString, Context.Target::toString), Context::target,
                Context::new
        );

        public sealed interface Target {
            @Nullable BinaryTag getNBT(@NotNull LootContext context);

            static @NotNull Target fromString(@NotNull String input) {
                if (input.equals("block_entity")) return new BlockEntity();

                for (var target : RelevantEntity.values()) {
                    if (target.id().equals(input)) return new Entity(target);
                }

                throw new IllegalArgumentException("Expected block_entity or a valid entity target name");
            }

            record BlockEntity() implements Target {
                @SuppressWarnings("DataFlowIssue")
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

                @Override
                public String toString() {
                    return "block_entity";
                }
            }

            record Entity(@NotNull RelevantEntity target) implements Target {
                @Override
                public @NotNull BinaryTag getNBT(@NotNull LootContext context) {
                    var entity = context.require(target.key());

                    return context.vanilla().serializeEntity(entity);
                }

                @Override
                public String toString() {
                    return target.id();
                }
            }
        }

        @Override
        public @Nullable BinaryTag getNBT(@NotNull LootContext context) {
            return target.getNBT(context);
        }
    }

}
