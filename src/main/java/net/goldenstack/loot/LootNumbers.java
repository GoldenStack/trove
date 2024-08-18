package net.goldenstack.loot;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class LootNumbers {

    private LootNumbers() {
        throw new UnsupportedOperationException();
    }

    public record Constant(@NotNull Number value) implements LootNumber {
        @Override
        public long getLong(@NotNull LootContext context) {
            return value.longValue();
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return value.doubleValue();
        }
    }

    public record Uniform(@NotNull LootNumber min, @NotNull LootNumber max) implements LootNumber {
        @Override
        public long getLong(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextLong(min().getLong(context), max().getLong(context) + 1);
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble(min().getDouble(context), max().getDouble(context));
        }
    }

    public record Binomial(@NotNull LootNumber trials, @NotNull LootNumber probability) implements LootNumber {
        @Override
        public long getLong(@NotNull LootContext context) {
            long trials = trials().getLong(context);
            double probability = probability().getDouble(context);
            Random random = context.require(LootContext.RANDOM);

            int successes = 0;
            for (int trial = 0; trial < trials; trial++) {
                if (random.nextDouble() < probability) {
                    successes++;
                }
            }
            return successes;
        }

        @Override
        public double getDouble(@NotNull LootContext context) {
            return getLong(context);
        }
    }

}
