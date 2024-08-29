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
            Template.entry("all_of", AllOf.class, AllOf.SERIALIZER),
            Template.entry("any_of", AnyOf.class, AnyOf.SERIALIZER),
            Template.entry("block_state_property", BlockStateProperty.class, BlockStateProperty.SERIALIZER),
            Template.entry("damage_source_properties", DamageSourceProperties.class, DamageSourceProperties.SERIALIZER),
            Template.entry("enchantment_active_check", EnchantmentActiveCheck.class, EnchantmentActiveCheck.SERIALIZER),
            Template.entry("entity_properties", EntityProperties.class, EntityProperties.SERIALIZER),
            Template.entry("entity_scores", EntityScores.class, EntityScores.SERIALIZER),
            Template.entry("inverted", Inverted.class, Inverted.SERIALIZER),
            Template.entry("killed_by_player", KilledByPlayer.class, KilledByPlayer.SERIALIZER),
            Template.entry("location_check", LocationCheck.class, LocationCheck.SERIALIZER),
            Template.entry("match_tool", MatchTool.class, MatchTool.SERIALIZER),
            Template.entry("random_chance", RandomChance.class, RandomChance.SERIALIZER),
            Template.entry("random_chance_with_enchanted_bonus", RandomChanceWithEnchantedBonus.class, RandomChanceWithEnchantedBonus.SERIALIZER),
            Template.entry("reference", Reference.class, Reference.SERIALIZER),
            Template.entry("survives_explosion", SurvivesExplosion.class, SurvivesExplosion.SERIALIZER),
            Template.entry("table_bonus", TableBonus.class, TableBonus.SERIALIZER),
            Template.entry("time_check", TimeCheck.class, TimeCheck.SERIALIZER),
            Template.entry("value_check", ValueCheck.class, ValueCheck.SERIALIZER),
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

    record AllOf(@NotNull List<LootPredicate> terms) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<AllOf> SERIALIZER = Template.template(
                "terms", Serial.lazy(() -> LootPredicate.SERIALIZER).list(), AllOf::terms,
                AllOf::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return all(terms, context);
        }
    }

    record AnyOf(@NotNull List<LootPredicate> terms) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<AnyOf> SERIALIZER = Template.template(
                "terms", Serial.lazy(() -> LootPredicate.SERIALIZER).list(), AnyOf::terms,
                AnyOf::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            if (terms.isEmpty()) {
                return false;
            }
            for (var predicate : terms) {
                if (predicate.test(context)) {
                    return true;
                }
            }
            return false;
        }
    }

    record Inverted(@NotNull LootPredicate term) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Inverted> SERIALIZER = Template.template(
                "term", Serial.lazy(() -> LootPredicate.SERIALIZER), Inverted::term,
                Inverted::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return !term.test(context);
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

    record RandomChance(@NotNull LootNumber chance) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<RandomChance> SERIALIZER = Template.template(
                "chance", LootNumber.SERIALIZER, RandomChance::chance,
                RandomChance::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble() < chance.getDouble(context);
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

    record ValueCheck(@NotNull LootNumber value, @NotNull LootNumberRange range) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<ValueCheck> SERIALIZER = Template.template(
                "value", LootNumber.SERIALIZER, ValueCheck::value,
                "range", LootNumberRange.SERIALIZER, ValueCheck::range,
                ValueCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return range.check(context, value.getInt(context));
        }
    }

    record TimeCheck(@Nullable Long period, @NotNull LootNumberRange value) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<TimeCheck> SERIALIZER = Template.template(
                "period", Serial.LONG.optional(), TimeCheck::period,
                "value", LootNumberRange.SERIALIZER, TimeCheck::value,
                TimeCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            long time = context.require(LootContext.WORLD).getTime();

            if (period != null) {
                time %= period;
            }

            return value.check(context, time);
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

    record Reference(@NotNull NamespaceID name) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<Reference> SERIALIZER = Template.template(
                "name", Serial.KEY, Reference::name,
                Reference::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            LootPredicate predicate = context.vanilla().getRegisteredPredicate(name);

            return predicate != null && predicate.test(context);
        }
    }

    record EnchantmentActiveCheck(boolean active) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<EnchantmentActiveCheck> SERIALIZER = Template.template(
                "active", BinaryTagSerializer.BOOLEAN, EnchantmentActiveCheck::active,
                EnchantmentActiveCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.ENCHANTMENT_ACTIVE) == active;
        }
    }

    record BlockStateProperty(@NotNull NamespaceID block, @Nullable BlockPredicate predicate) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<BlockStateProperty> SERIALIZER = Template.template(
                "block", Serial.KEY, BlockStateProperty::block,
                "properties", BlockPredicate.SERIALIZER.optional(), BlockStateProperty::predicate,
                BlockStateProperty::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);

            return block != null && this.block.equals(block.namespace()) && (predicate == null || predicate.test(block));
        }
    }

    record DamageSourceProperties(@Nullable DamageSourcePredicate predicate) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<DamageSourceProperties> SERIALIZER = Template.template(
                "predicate",Serial.lazy(DamageSourcePredicate.SERIALIZER::get).optional(), DamageSourceProperties::predicate,
                DamageSourceProperties::new
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

    record EntityProperties(@Nullable EntityPredicate predicate, @NotNull RelevantEntity entity) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<EntityProperties> SERIALIZER = Template.template(
                "predicate", Serial.lazy(EntityPredicate.SERIALIZER::get), EntityProperties::predicate,
                "entity", RelevantEntity.SERIALIZER, EntityProperties::entity,
                EntityProperties::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(this.entity.key());
            Point origin = context.get(LootContext.ORIGIN);

            return predicate == null || predicate.test(context.require(LootContext.WORLD), origin, entity);
        }
    }

    record LocationCheck(@Nullable LocationPredicate predicate, double offsetX, double offsetY, double offsetZ) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<LocationCheck> SERIALIZER = Template.template(
                "predicate", Serial.lazy(LocationPredicate.SERIALIZER::get), LocationCheck::predicate,
                "offsetX", Serial.DOUBLE.optional(0D), LocationCheck::offsetX,
                "offsetY", Serial.DOUBLE.optional(0D), LocationCheck::offsetY,
                "offsetZ", Serial.DOUBLE.optional(0D), LocationCheck::offsetZ,
                LocationCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Point origin = context.get(LootContext.ORIGIN);

            if (origin == null) return false;
            if (predicate == null) return true;

            return predicate.test(context.require(LootContext.WORLD), origin.add(offsetX, offsetY, offsetZ));
        }
    }

    record MatchTool(@Nullable ItemPredicate predicate) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<MatchTool> SERIALIZER = Template.template(
                "predicate", Serial.lazy(ItemPredicate.SERIALIZER::get), MatchTool::predicate,
                MatchTool::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            if (tool == null) return false;
            if (predicate == null) return true;

            return predicate.test(tool);
        }
    }

    record EntityScores(@NotNull Map<String, LootNumberRange> scores, @NotNull RelevantEntity entity) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<EntityScores> SERIALIZER = Template.template(
                "scores", Serial.map(LootNumberRange.SERIALIZER), EntityScores::scores,
                "entity", RelevantEntity.SERIALIZER, EntityScores::entity,
                EntityScores::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(this.entity.key());
            if (entity == null) return false;

            for (var entry : scores.entrySet()) {
                Integer score = context.vanilla().getScore(entity, entry.getKey());
                if (score == null || !entry.getValue().check(context, score)) {
                    return false;
                }
            }

            return true;
        }
    }

    record RandomChanceWithEnchantedBonus(@NotNull DynamicRegistry.Key<Enchantment> enchantment, float unenchantedChance, @NotNull LevelBasedValue enchantedChance) implements LootPredicate {

        public static final @NotNull BinaryTagSerializer<RandomChanceWithEnchantedBonus> SERIALIZER = Template.template(
                "enchantment", Serial.key(), RandomChanceWithEnchantedBonus::enchantment,
                "unenchanted_chance", BinaryTagSerializer.FLOAT, RandomChanceWithEnchantedBonus::unenchantedChance,
                "enchanted_chance", LevelBasedValue.NBT_TYPE, RandomChanceWithEnchantedBonus::enchantedChance,
                RandomChanceWithEnchantedBonus::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity attacker = context.get(LootContext.ATTACKING_ENTITY);

            int level = EnchantmentUtils.level(attacker, enchantment);

            float chance = level > 0 ? enchantedChance.calc(level) : unenchantedChance;
            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }
    }

}

