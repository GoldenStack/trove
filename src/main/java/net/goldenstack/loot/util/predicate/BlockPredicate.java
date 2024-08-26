package net.goldenstack.loot.util.predicate;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record BlockPredicate(@NotNull List<SingularCheck> checks) implements Predicate<@NotNull Block> {

    public sealed interface SingularCheck extends Predicate<@NotNull Block> permits IdenticalState, RangedLongState {

        @NotNull String key();

    }

    /**
     * Assures that the matching state is equal to {@link #value()}.
     * @param key the key of this check
     * @param value the exact required value of the check
     */
    public record IdenticalState(@NotNull String key, @NotNull String value) implements SingularCheck {

        @Override
        public boolean test(@NotNull Block block) {
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
        public boolean test(@NotNull Block block) {
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

    public BlockPredicate {
        checks = List.copyOf(checks);
    }

    @Override
    public boolean test(@NotNull Block block) {
        if (checks.isEmpty()) {
            return true;
        }

        for (SingularCheck check : checks) {
            if (!check.test(block)) {
                return false;
            }
        }
        return true;
    }
}
