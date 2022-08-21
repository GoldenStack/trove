package dev.goldenstack.loot.minestom.util.check;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.Scalars;

import java.util.ArrayList;
import java.util.List;

/**
 * A check that verifies the state of a block.
 * @param checks the checks that must all pass
 */
public record BlockStateCheck(@NotNull List<SingularCheck> checks) {

    /**
     * Attempts to deserialize the provided configuration node into a BlockStateCheck.
     * @param node the node to deserialize
     * @return the deserialized block state check
     * @throws ConfigurateException if the node could not be deserialized into a valid block state check
     */
    public static @NotNull BlockStateCheck deserialize(@NotNull ConfigurationNode node) throws ConfigurateException {
        if (node.empty()) {
            return new BlockStateCheck(List.of());
        }
        if (!node.isMap()) {
            throw new ConfigurateException(node, "Expected the node to be a map, but found another type");
        }
        List<SingularCheck> checks = new ArrayList<>();
        for (var entry : node.childrenMap().entrySet()) {
            if (entry.getValue().isMap()) {
                checks.add(new RangedLongState(
                        String.valueOf(entry.getKey()),
                        Scalars.LONG.tryDeserialize(entry.getValue().node("min").rawScalar()),
                        Scalars.LONG.tryDeserialize(entry.getValue().node("max").rawScalar())
                ));
            } else {
                var scalar = entry.getValue().rawScalar();
                if (scalar == null) {
                    throw new ConfigurateException(entry.getValue(), "Expected the node to be a scalar or a map, but found another type");
                }
                checks.add(new IdenticalState(String.valueOf(entry.getKey()), String.valueOf(scalar)));
            }
        }
        return new BlockStateCheck(checks);
    }

    /**
     * Attempts to serialize the provided block state check onto the provided configuration node.
     * @param check the check to serialize
     * @param node the node to serialize properties onto
     * @throws ConfigurateException if the check could not be serialized onto the provided node for some reason
     */
    public static void serialize(@NotNull BlockStateCheck check, @NotNull ConfigurationNode node) throws ConfigurateException {
        if (check.checks.isEmpty()) {
            return;
        }
        for (var singular : check.checks) {
            var child = node.node(singular.key());
            if (singular instanceof IdenticalState identicalState) {
                child.set(identicalState.value);
            } else if (singular instanceof RangedLongState rangedLongState) {
                child.node("min").set(rangedLongState.min);
                child.node("max").set(rangedLongState.max);
            } else {
                throw new ConfigurateException("Expected check '" + singular + "' to be an identical check or a ranged check, but found neither!");
            }
        }
    }

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
