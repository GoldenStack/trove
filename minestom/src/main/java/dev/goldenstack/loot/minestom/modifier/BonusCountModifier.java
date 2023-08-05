package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.generator.SerializerSelector;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;
import java.util.Random;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * Modifies the count of each provided item based on the {@link #bonus()} and the enchantment level on the
 * {@link LootContextKeys#TOOL}.
 * @param conditions the conditions required for modification
 * @param addedEnchantment the enchantment to be given to the bonus finder
 * @param bonus the equation that determines the bonus based on the provided factors
 */
public record BonusCountModifier(@NotNull List<LootCondition> conditions,
                                 @NotNull Enchantment addedEnchantment,
                                 @NotNull BonusType bonus) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:apply_bonus";

    /**
     * A standard map-based converter for bonus count modifiers.
     */
    public static final @NotNull TypeSerializer<BonusCountModifier> CONVERTER =
            converter(BonusCountModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(Enchantment.class).name("addedEnchantment").nodePath("enchantment"),
                    field(BonusType.class).name("bonus").nodePath(List.of())
            );

    /**
     * Represents an arbitrary value generator based on a random number generator, a current count, and an enchantment
     * level.
     */
    public interface BonusType {

        TypeSerializer<BonusType> TYPE_CONVERTER = new SerializerSelector<>(TypeToken.get(BonusType.class))
                .keyLocation("formula")
                .add(BinomialBonus.KEY, BinomialBonus.class)
                .add(UniformBonus.KEY, UniformBonus.class)
                .add(FortuneDrops.KEY, FortuneDrops.class)
                .build();

        int calculateNewValue(Random random, int count, int enchantmentLevel);

    }

    /**
     * Manages a bonus effect where the distribution of possible bonus count falls under a binomial distribution that is
     * determined largely by {@link #probability()}
     * @param levelBonus the constant that, when added to the enchantment level, represents the number of trials
     * @param probability the probability that each trial succeeds
     */
    public record BinomialBonus(int levelBonus, float probability) implements BonusType {

        public static final @NotNull String KEY = "minecraft:binomial_with_bonus_count";

        public static final @NotNull TypeSerializer<BinomialBonus> CONVERTER =
                converter(BinomialBonus.class,
                        field(int.class).name("levelBonus").nodePath("parameters", "extra"),
                        field(float.class).name("probability").nodePath("parameters", "probability")
                );

        @Override
        public int calculateNewValue(Random random, int count, int enchantmentLevel) {
            for (int i = 0; i < enchantmentLevel + levelBonus; i++) {
                if (random.nextDouble() < probability) {
                    count++;
                }
            }
            return count;
        }

    }

    /**
     * Manages a simple bonus effect, where a bonus count is multiplied by the enchantment level.
     * @param multiplier the number that is multiplied by the enchantment level when determining a cap for the random
     *                   number
     */
    public record UniformBonus(int multiplier) implements BonusType {

        public static final @NotNull String KEY = "minecraft:uniform_bonus_count";

        public static final @NotNull TypeSerializer<UniformBonus> CONVERTER =
                converter(UniformBonus.class,
                        field(int.class).name("multiplier").nodePath("parameters", "bonusMultiplier")
                );

        @Override
        public int calculateNewValue(Random random, int count, int enchantmentLevel) {
            return count + random.nextInt(1 + multiplier * enchantmentLevel);
        }

    }

    /**
     * Manages the fortune effect, where the number of dropped items is multiplied by a certain value when there's a
     * higher level of fortune.
     */
    public record FortuneDrops() implements BonusType {

        public static final @NotNull String KEY = "minecraft:ore_drops";

        public static final @NotNull TypeSerializer<FortuneDrops> CONVERTER =
                converter(FortuneDrops.class);

        @Override
        public int calculateNewValue(Random random, int count, int enchantmentLevel) {
            if (enchantmentLevel <= 0) {
                return count;
            }

            int multiplier = Math.max(1, random.nextInt(enchantmentLevel + 2));
            return count * multiplier;
        }

    }

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context) || !context.has(LootContextKeys.TOOL)) {
            return input;
        }

        ItemStack tool = context.assure(LootContextKeys.TOOL);
        int level = tool.meta().getEnchantmentMap().getOrDefault(addedEnchantment, (short) 0);
        return input.withAmount(this.bonus.calculateNewValue(context.random(), input.amount(), level));
    }

}
