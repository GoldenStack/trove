package net.goldenstack.loot;

import net.goldenstack.loot.util.*;
import net.goldenstack.loot.util.predicate.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.item.enchant.LevelBasedValue;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A predicate over a loot context, returning whether or not a given context passes some arbitrary predicate.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootPredicate extends Predicate<@NotNull LootContext> {

    @NotNull BinaryTagSerializer<LootPredicate> SERIALIZER = Template.registry("condition",
            Template.entry("all_of", All.class, All.SERIALIZER),
            Template.entry("any_of", Any.class, Any.SERIALIZER),
            Template.entry("block_state_property", BlockState.class, BlockState.SERIALIZER),
            Template.entry("damage_source_properties", DamageSource.class, DamageSource.SERIALIZER),
            Template.entry("enchantment_active_check", EnchantmentActive.class, EnchantmentActive.SERIALIZER),
            Template.entry("entity_properties", EntityProperties.class, EntityProperties.SERIALIZER),
            Template.entry("entity_scores", Scores.class, Scores.SERIALIZER),
            Template.entry("inverted", Inverted.class, Inverted.SERIALIZER),
            Template.entry("killed_by_player", KilledByPlayer.class, KilledByPlayer.SERIALIZER),
            Template.entry("location_check", Location.class, Location.SERIALIZER),
            Template.entry("match_tool", Tool.class, Tool.SERIALIZER),
            Template.entry("random_chance", RandomChance.class, RandomChance.SERIALIZER),
            Template.entry("random_chance_with_enchanted_bonus", EnchantmentBasedRandomChance.class, EnchantmentBasedRandomChance.SERIALIZER),
            Template.entry("reference", Reference.class, Reference.SERIALIZER),
            Template.entry("survives_explosion", SurvivesExplosion.class, SurvivesExplosion.SERIALIZER),
            Template.entry("table_bonus", TableBonus.class, TableBonus.SERIALIZER),
            Template.entry("time_check", TimeCheck.class, TimeCheck.SERIALIZER),
            Template.entry("value_check", RangeCheck.class, RangeCheck.SERIALIZER),
            Template.entry("weather_check", WeatherCheck.class, WeatherCheck.SERIALIZER)
    );

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

        public static final @NotNull BinaryTagSerializer<All> SERIALIZER = Template.template(
                "terms", Serial.lazy(() -> LootPredicate.SERIALIZER).list(), All::predicates,
                All::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return all(predicates, context);
        }
    }

    record Any(@NotNull List<LootPredicate> predicates) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Any> SERIALIZER = Template.template(
                "terms", Serial.lazy(() -> LootPredicate.SERIALIZER).list(), Any::predicates,
                Any::new
        );

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

        public static final @NotNull BinaryTagSerializer<Inverted> SERIALIZER = Template.template(
                "term", Serial.lazy(() -> LootPredicate.SERIALIZER), Inverted::child,
                Inverted::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return !child.test(context);
        }
    }

    record SurvivesExplosion() implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<SurvivesExplosion> SERIALIZER = Template.template(SurvivesExplosion::new);

        @Override
        public boolean test(@NotNull LootContext context) {
            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            return radius == null || context.require(LootContext.RANDOM).nextFloat() <= (1 / radius);
        }
    }

    record KilledByPlayer() implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<KilledByPlayer> SERIALIZER = Template.template(KilledByPlayer::new);

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.has(LootContext.LAST_DAMAGE_PLAYER);
        }
    }

    record RandomChance(@NotNull LootNumber number) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<RandomChance> SERIALIZER = Template.template(
                "chance", LootNumber.SERIALIZER, RandomChance::number,
                RandomChance::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble() < number.getDouble(context);
        }
    }

    record WeatherCheck(@Nullable Boolean raining, @Nullable Boolean thundering) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<WeatherCheck> SERIALIZER = Template.template(
                "raining", BinaryTagSerializer.BOOLEAN.optional(), WeatherCheck::raining,
                "thundering", BinaryTagSerializer.BOOLEAN.optional(), WeatherCheck::thundering,
                WeatherCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Weather weather = context.require(LootContext.WORLD).getWeather();

            return (raining == null || raining == weather.isRaining()) &&
                    (thundering == null || thundering == weather.thunderLevel() > 0);
        }
    }

    record RangeCheck(@NotNull LootNumber source, @NotNull LootNumberRange range) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<RangeCheck> SERIALIZER = Template.template(
                "value", LootNumber.SERIALIZER, RangeCheck::source,
                "range", LootNumberRange.SERIALIZER, RangeCheck::range,
                RangeCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return range.check(context, source.getInt(context));
        }
    }

    record TimeCheck(@Nullable Long period, @NotNull LootNumberRange range) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<TimeCheck> SERIALIZER = Template.template(
                "period", Serial.LONG.optional(), TimeCheck::period,
                "value", LootNumberRange.SERIALIZER, TimeCheck::range,
                TimeCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            long time = context.require(LootContext.WORLD).getTime();

            if (period != null) {
                time %= period;
            }

            return range.check(context, time);
        }
    }

    record TableBonus(@NotNull DynamicRegistry.Key<Enchantment> enchantment, @NotNull List<Float> chances) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<TableBonus> SERIALIZER = Template.template(
                "enchantment", Serial.key(), TableBonus::enchantment,
                "chances", BinaryTagSerializer.FLOAT.list(), TableBonus::chances,
                TableBonus::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            int level = EnchantmentUtils.level(tool, enchantment);

            float chance = chances.get(Math.min(this.chances.size() - 1, level));

            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }
    }

    record Reference(@NotNull NamespaceID key) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Reference> SERIALIZER = Template.template(
                "name", Serial.KEY, Reference::key,
                Reference::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            LootPredicate predicate = context.require(LootContext.REGISTERED_PREDICATES).apply(key);

            return predicate != null && predicate.test(context);
        }
    }

    record EnchantmentActive(boolean active) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<EnchantmentActive> SERIALIZER = Template.template(
                "active", BinaryTagSerializer.BOOLEAN, EnchantmentActive::active,
                EnchantmentActive::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.ENCHANTMENT_ACTIVE) == active;
        }
    }

    record BlockState(@NotNull NamespaceID key, @Nullable BlockPredicate predicate) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<BlockState> SERIALIZER = Template.template(
                "block", Serial.KEY, BlockState::key,
                "properties", BlockPredicate.SERIALIZER.optional(), BlockState::predicate,
                BlockState::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);

            return block != null && key.equals(block.namespace()) && (predicate == null || predicate.test(block));
        }
    }

    record DamageSource(@Nullable DamageSourcePredicate predicate) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<DamageSource> SERIALIZER = Template.template(
                "predicate",Serial.lazy(DamageSourcePredicate.SERIALIZER::get).optional(), DamageSource::predicate,
                DamageSource::new
        );

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

        public static final @NotNull BinaryTagSerializer<EntityProperties> SERIALIZER = Template.template(
                "predicate", Serial.lazy(EntityPredicate.SERIALIZER::get), EntityProperties::predicate,
                "entity", RelevantEntity.SERIALIZER, EntityProperties::relevantEntity,
                EntityProperties::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(relevantEntity.key());
            Point origin = context.get(LootContext.ORIGIN);

            return predicate == null || predicate.test(context.require(LootContext.WORLD), origin, entity);
        }
    }

    record Location(@Nullable LocationPredicate predicate, double offsetX, double offsetY, double offsetZ) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Location> SERIALIZER = Template.template(
                "predicate", Serial.lazy(LocationPredicate.SERIALIZER::get), Location::predicate,
                "offsetX", Serial.DOUBLE.optional(0D), Location::offsetX,
                "offsetY", Serial.DOUBLE.optional(0D), Location::offsetY,
                "offsetZ", Serial.DOUBLE.optional(0D), Location::offsetZ,
                Location::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Point origin = context.get(LootContext.ORIGIN);

            if (origin == null) return false;
            if (predicate == null) return true;

            return predicate.test(context.require(LootContext.WORLD), origin.add(offsetX, offsetY, offsetZ));
        }
    }

    record Tool(@Nullable ItemPredicate predicate) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Tool> SERIALIZER = Template.template(
                "predicate", Serial.lazy(ItemPredicate.SERIALIZER::get), Tool::predicate,
                Tool::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            if (tool == null) return false;
            if (predicate == null) return true;

            return predicate.test(tool);
        }
    }

    record Scores(@NotNull Map<String, LootNumberRange> scores, @NotNull RelevantEntity relevantEntity) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Scores> SERIALIZER = Template.template(
                "scores", Serial.map(LootNumberRange.SERIALIZER), Scores::scores,
                "entity", RelevantEntity.SERIALIZER, Scores::relevantEntity,
                Scores::new
        );

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

    record EnchantmentBasedRandomChance(@NotNull DynamicRegistry.Key<Enchantment> enchantment, float defaultChance, @NotNull LevelBasedValue modifiedChance) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<EnchantmentBasedRandomChance> SERIALIZER = Template.template(
                "enchantment", Serial.key(), EnchantmentBasedRandomChance::enchantment,
                "unenchanted_chance", BinaryTagSerializer.FLOAT, EnchantmentBasedRandomChance::defaultChance,
                "enchanted_chance", LevelBasedValue.NBT_TYPE, EnchantmentBasedRandomChance::modifiedChance,
                EnchantmentBasedRandomChance::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity attacker = context.get(LootContext.ATTACKING_ENTITY);

            int level = EnchantmentUtils.level(attacker, enchantment);

            float chance = level > 0 ? modifiedChance.calc(level) : defaultChance;
            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }
    }

}

