package dev.goldenstack.loot.minestom.util.check;

import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

/**
 * A check that verifies the state of a block. See {@link #verify(Block)} for details.
 * @param checks the checks that must all pass
 */
public record BlockStateCheck(@NotNull List<SingularCheck> checks) {

    /**
     * A standard map-based serializer for block state checks.
     */
    public static final @NotNull TypedLootConverter<BlockStateCheck> CONVERTER = TypedLootConverter.join(BlockStateCheck.class,
            (input, result) -> {
                if (input.checks.isEmpty()) {
                    return;
                }
                for (var singular : input.checks) {
                    var child = result.node(singular.key());
                    if (singular instanceof IdenticalState identicalState) {
                        child.set(identicalState.value);
                    } else if (singular instanceof RangedLongState rangedLongState) {
                        child.node("min").set(rangedLongState.min);
                        child.node("max").set(rangedLongState.max);
                    } else {
                        throw new SerializationException(result, singular.getClass(), "Cannot serialize unknown type");
                    }
                }
            }, input -> {
                if (input.isNull()) {
                    return new BlockStateCheck(List.of());
                }
                if (!input.isMap()) {
                    throw new SerializationException(input, BlockStateCheck.class, "Expected a map");
                }
                List<SingularCheck> checks = new ArrayList<>();
                for (var entry : input.childrenMap().entrySet()) {
                    if (entry.getValue().isMap()) {
                        checks.add(new RangedLongState(
                                String.valueOf(entry.getKey()),
                                entry.getValue().node("min").get(Long.class),
                                entry.getValue().node("max").get(Long.class)
                        ));
                    } else {
                        var scalar = entry.getValue().rawScalar();
                        if (scalar == null) {
                            throw new SerializationException(entry.getValue(), SingularCheck.class, "Expected a scalar or a map");
                        }
                        checks.add(new IdenticalState(String.valueOf(entry.getKey()), String.valueOf(scalar)));
                    }
                }
                return new BlockStateCheck(checks);
            }
    );

    /**
     * The base check, which has a key and a simple `check` method.
     */
    public sealed interface SingularCheck permits IdenticalState, RangedLongState {

        /**
         * The key of this check, which is used for serialization as well as possibly internal use
         * @return this check's key
         */
        @NotNull String key();

        /**
         * Checks to see if the provided block individually passes this check
         * @param block the block to check
         * @return true if it passed, and false otherwise
         */
        boolean check(@NotNull Block block);

    }

    /**
     * Assures that the matching state is equal to {@link #value()}.
     * @param key the key of this check
     * @param value the exact required value of the check
     */
    public record IdenticalState(@NotNull String key, @NotNull String value) implements SingularCheck {

        @Override
        public boolean check(@NotNull Block block) {
            return value.equals(block.getProperty(key));
        }

    }

    /**
     * Assures that the matching state is within the (optional) range of {@link #min()} and {@link #max()}.
     * @param key the key of this check
     * @param min the (optional) minimum value
     * @param max the (optional) maximum value
     */
    public record RangedLongState(@NotNull String key, @Nullable Long min, @Nullable Long max) implements SingularCheck {

        @Override
        public boolean check(@NotNull Block block) {
            String value = block.getProperty(key);
            if (value == null) {
                return false;
            }
            try {
                long parsedValue = Long.parseLong(value);
                return (min == null || parsedValue >= min) && (max == null || parsedValue <= max);
            } catch (NumberFormatException exception) {
                return false;
            }
        }

    }

    public BlockStateCheck {
        checks = List.copyOf(checks);
    }

    /**
     * Individually verifies each check in {@link #checks()} with the provided block, and returns false if any of them
     * do.
     * @param block the block to check
     * @return true if the provided block was verified, and false if not
     */
    public boolean verify(@NotNull Block block) {
        if (checks.isEmpty()) {
            return true;
        }
        for (SingularCheck check : checks) {
            if (!check.check(block)) {
                return false;
            }
        }
        return true;
    }

}
