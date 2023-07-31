package dev.goldenstack.loot.minestom.nbt;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.ConditionalLootConverter;
import dev.goldenstack.loot.converter.generator.Converters;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.RelevantEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Optional;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.type;

/**
 * Retrieves NBT based on some information from the provided context.
 * @param target the target that acts as the source of this context NBT
 */
public record ContextNBT(@NotNull NBTTarget target) implements LootNBT {

    /**
     * A converter for constant NBT that always serializes to a string scalar and deserializes when the input is a
     * single string scalar.
     */
    public static final @NotNull ConditionalLootConverter<LootNBT> ACCURATE_CONVERTER = ConditionalLootConverter.join(
            (input, result) -> {
                if (input instanceof ContextNBT contextNBT) {
                    result.set(contextNBT.target().serializedString());
                }
            }, input -> {
                if (input.rawScalar() instanceof String string) {
                    var target = fromString(string);
                    if (target == null) {
                        throw new SerializationException(input, ContextNBT.NBTTarget.class, "Could not read block entity or a RelevantEntity from the provided node");
                    }
                    return Optional.of(new ContextNBT(target));
                }
                return Optional.empty();
            }
    );

    public static final @NotNull String KEY = "minecraft:context";

    /**
     * A standard map-based converter for context NBT providers.
     */
    public static final @NotNull TypedLootConverter<ContextNBT> CONVERTER =
            converter(ContextNBT.class,
                    type(NBTTarget.class).name("target")
            );

    public static final @NotNull TypedLootConverter<NBTTarget> TARGET_CONVERTER = Converters.proxied(String.class, NBTTarget.class, ContextNBT::fromString, NBTTarget::serializedString);

    private static @Nullable NBTTarget fromString(@NotNull String id) {
        if (id.equals("block_entity")) {
            return new BlockEntityTarget();
        }
        var relevant = RelevantEntity.ofId(id);

        return relevant != null ? new EntityTarget(relevant) : null;
    }

    /**
     * Represents two types of targets that can provide NBT differently.
     */
    public sealed interface NBTTarget permits BlockEntityTarget, EntityTarget {

        /**
         * Retrieves NBT from the provided context.
         * @param context the context to use
         * @return the NBT retrieved from the context
         */
        @Nullable NBT getNBT(@NotNull LootContext context);

        /**
         * Converts this NBT target back into a string.
         * @return this target, as a string
         */
        @NotNull String serializedString();

    }

    public record BlockEntityTarget() implements NBTTarget {

        @Override
        public @NotNull NBT getNBT(@NotNull LootContext context) {
            var blockEntity = context.assure(LootContextKeys.BLOCK_ENTITY);

            var pos = blockEntity.position();

            NBTCompound nbt = blockEntity.block().hasNbt() ? blockEntity.block().nbt() : new NBTCompound();

            nbt = nbt.modify(mut -> {
                mut.setInt("x", pos.blockX());
                mut.setInt("y", pos.blockY());
                mut.setInt("z", pos.blockZ());

                mut.setString("id", blockEntity.block().namespace().asString());
            });

            return nbt;
        }
        
        @Override
        public @NotNull String serializedString() {
            return "block_entity";
        }
    }

    public record EntityTarget(@NotNull RelevantEntity target) implements NBTTarget {

        @Override
        public @NotNull NBT getNBT(@NotNull LootContext context) {
            var entity = context.assure(target.key());
            var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);

            return vanilla.getEntityNBT(entity);
        }

        @Override
        public @NotNull String serializedString() {
            return target.id();
        }
    }

    @Override
    public @Nullable NBT getNBT(@NotNull LootContext context) {
        return target.getNBT(context);
    }

}
