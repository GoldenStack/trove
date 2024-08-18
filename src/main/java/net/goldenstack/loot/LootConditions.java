package net.goldenstack.loot;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LootConditions {

    private LootConditions() {
        throw new UnsupportedOperationException();
    }

    public record All(@NotNull List<LootCondition> conditions) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return LootCondition.all(conditions, context);
        }
    }

    public record Any(@NotNull List<LootCondition> conditions) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return LootCondition.any(conditions, context);
        }
    }

    public record Inverted(@NotNull LootCondition child) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return !child.test(context);
        }
    }

    public record SurvivesExplosion() implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            return radius == null || context.require(LootContext.RANDOM).nextFloat() <= (1 / radius);
        }
    }

    public record KilledByPlayer() implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return context.has(LootContext.LAST_DAMAGE_PLAYER);
        }
    }

}
