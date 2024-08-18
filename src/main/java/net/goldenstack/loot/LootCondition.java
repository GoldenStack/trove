package net.goldenstack.loot;

import net.minestom.server.instance.Weather;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * A predicate over a loot context, returning whether or not a given context passes some arbitrary condition.
 */
public interface LootCondition extends Predicate<@NotNull LootContext> {

    /**
     * Returns whether or not the provided loot context passes this condition's predicate.
     * @param context the context object, to use if required
     * @return true if the provided loot context is valid according to this condition
     */
    @Override
    boolean test(@NotNull LootContext context);

    record All(@NotNull List<LootCondition> conditions) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            if (conditions.isEmpty()) {
                return true;
            }
            for (var condition : conditions) {
                if (!condition.test(context)) {
                    return false;
                }
            }
            return true;
        }
    }

    record Any(@NotNull List<LootCondition> conditions) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            if (conditions.isEmpty()) {
                return false;
            }
            for (var condition : conditions) {
                if (condition.test(context)) {
                    return true;
                }
            }
            return false;
        }
    }

    record Inverted(@NotNull LootCondition child) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return !child.test(context);
        }
    }

    record SurvivesExplosion() implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            return radius == null || context.require(LootContext.RANDOM).nextFloat() <= (1 / radius);
        }
    }

    record KilledByPlayer() implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return context.has(LootContext.LAST_DAMAGE_PLAYER);
        }
    }

    record RandomChance(@NotNull LootNumber number) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble() < number.getDouble(context);
        }
    }

    record WeatherCheck(@Nullable Boolean raining, @Nullable Boolean thundering) implements LootCondition {
        @Override
        public boolean test(@NotNull LootContext context) {
            Weather weather = context.require(LootContext.WORLD).getWeather();

            return (raining == null || raining == weather.isRaining()) &&
                    (thundering == null || thundering == weather.thunderLevel() > 0);
        }
    }



}

