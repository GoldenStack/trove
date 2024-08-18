package net.goldenstack.loot;

import net.goldenstack.loot.util.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.LevelBasedValue;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A predicate over a loot context, returning whether or not a given context passes some arbitrary predicate.
 */
public interface LootPredicate extends Predicate<@NotNull LootContext> {

    /**
     * Returns whether or not the provided loot context passes this predicate.
     * @param context the context object, to use if required
     * @return true if the provided loot context is valid according to this predicate
     */
    @Override
    boolean test(@NotNull LootContext context);

    /**
     * Returns whether or not every given predicate verifies the provided context.
     */
    static boolean all(@NotNull List<LootPredicate> predicates, @NotNull LootContext context) {
        if (predicates.isEmpty()) {
            return true;
        }
        for (var predicate : predicates) {
            if (!predicate.test(context)) {
                return false;
            }
        }
        return true;
    }

    record All(@NotNull List<LootPredicate> predicates) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            return all(predicates, context);
        }
    }

    record Any(@NotNull List<LootPredicate> predicates) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            if (predicates.isEmpty()) {
                return false;
            }
            for (var predicate : predicates) {
                if (predicate.test(context)) {
                    return true;
                }
            }
            return false;
        }
    }

    record Inverted(@NotNull LootPredicate child) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            return !child.test(context);
        }
    }

    record SurvivesExplosion() implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            return radius == null || context.require(LootContext.RANDOM).nextFloat() <= (1 / radius);
        }
    }

    record KilledByPlayer() implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            return context.has(LootContext.LAST_DAMAGE_PLAYER);
        }
    }

    record RandomChance(@NotNull LootNumber number) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble() < number.getDouble(context);
        }
    }

    record WeatherCheck(@Nullable Boolean raining, @Nullable Boolean thundering) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Weather weather = context.require(LootContext.WORLD).getWeather();

            return (raining == null || raining == weather.isRaining()) &&
                    (thundering == null || thundering == weather.thunderLevel() > 0);
        }
    }

    record RangeCheck(@NotNull LootNumber source, @NotNull LootNumberRange range) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            return range.check(context, source.getLong(context));
        }
    }

    record TimeCheck(@Nullable Long period, @NotNull LootNumberRange range) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            long time = context.require(LootContext.WORLD).getTime();

            if (period != null) {
                time %= period;
            }

            return range.check(context, time);
        }
    }

    record EnchantmentBonus(@NotNull NamespaceID enchantment, @NotNull List<Float> chances) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            int level = 0;

            if (tool != null) {
                EnchantmentList enchantments = tool.get(ItemComponent.ENCHANTMENTS);
                if (enchantments != null) {
                    level = enchantments.enchantments().getOrDefault(DynamicRegistry.Key.of(enchantment), 0);
                }
            }

            float chance = chances.get(Math.min(this.chances.size() - 1, level));

            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }
    }

    record Reference(@NotNull NamespaceID key) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            LootPredicate predicate = context.require(LootContext.REGISTERED_PREDICATES).apply(key);

            return predicate != null && predicate.test(context);
        }
    }

    record EnchantmentActive(boolean active) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.ENCHANTMENT_ACTIVE) == active;
        }
    }

    record BlockState(@NotNull NamespaceID key, @Nullable BlockPredicate predicate) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);

            return block != null && key.equals(block.namespace()) && (predicate == null || predicate.test(block));
        }
    }

    record DamageSource(@Nullable DamageSourcePredicate predicate) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Instance world = context.get(LootContext.WORLD);
            Point origin = context.get(LootContext.ORIGIN);
            DamageType damage = context.get(LootContext.DAMAGE_SOURCE);

            if (predicate == null || world == null || origin == null || damage == null) {
                return false;
            }

            return predicate.test(world, origin, damage);
        }
    }

    record EntityProperties(@Nullable EntityPredicate predicate, @NotNull RelevantEntity relevantEntity) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(relevantEntity.key());
            Point origin = context.get(LootContext.ORIGIN);

            return predicate == null || predicate.test(context.require(LootContext.WORLD), origin, entity);
        }
    }

    record Location(@Nullable LocationPredicate predicate, @NotNull Point offset) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Point origin = context.get(LootContext.ORIGIN);

            if (origin == null) return false;
            if (predicate == null) return true;

            return predicate.test(context.require(LootContext.WORLD), origin.add(offset));
        }
    }

    record Tool(@Nullable ItemPredicate predicate) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            if (tool == null) return false;
            if (predicate == null) return true;

            return predicate.test(tool);
        }
    }

    record Scores(@NotNull Map<String, LootNumberRange> scores, @NotNull RelevantEntity relevantEntity) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(relevantEntity.key());
            if (entity == null) return false;

            VanillaInterface vanilla = context.require(LootContext.VANILLA_INTERFACE);

            for (var entry : scores.entrySet()) {
                Integer score = vanilla.getScore(entity, entry.getKey());
                if (score == null || !entry.getValue().check(context, score)) {
                    return false;
                }
            }

            return true;
        }
    }

    record EnchantmentBasedRandomChance(@NotNull NamespaceID key, float defaultChance, @NotNull LevelBasedValue modifiedChance) implements LootPredicate {
        @Override
        public boolean test(@NotNull LootContext context) {
            Entity attacker = context.get(LootContext.ATTACKING_ENTITY);

            int level = 0;
            if (attacker instanceof LivingEntity living) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    EnchantmentList ench = living.getEquipment(slot).get(ItemComponent.ENCHANTMENTS);
                    if (ench == null) continue;

                    level = Math.max(level, ench.level(DynamicRegistry.Key.of(key)));
                }
            }

            float chance = level > 0 ? modifiedChance.calc(level) : defaultChance;
            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }
    }

}

