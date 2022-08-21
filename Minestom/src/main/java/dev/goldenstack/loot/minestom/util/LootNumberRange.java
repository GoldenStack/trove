package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * An inclusive number range based on loot numbers.
 * @param min the optional minimum value
 * @param max the optional maximum value
 */
public record LootNumberRange(@Nullable LootNumber<ItemStack> min, @Nullable LootNumber<ItemStack> max) {

    /**
     * A standard converter for loot number ranges that deserializes any numerical scalars to a range consisting of two
     * equivalent {@link ConstantNumber} instances holding the number, but otherwise handles it normally with map-based
     * values.
     */
    public static final @NotNull LootConverter<ItemStack, LootNumberRange> CONVERTER = new LootConverter<>() {
        @Override
        public @NotNull LootNumberRange deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            var number = input.get(Double.class);
            if (number != null) {
                var constant = new ConstantNumber(number);
                return new LootNumberRange(constant, constant);
            }
            return new LootNumberRange(
                    context.loader().lootNumberManager().deserialize(input.node("min"), context),
                    context.loader().lootNumberManager().deserialize(input.node("max"), context)
            );
        }

        @Override
        public @NotNull ConfigurationNode serialize(@NotNull LootNumberRange input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            var node = context.loader().createNode();
            if (input.min != null) {
                node.node("min").set(context.loader().lootNumberManager().serialize(input.min, context));
            }
            if (input.max != null) {
                node.node("max").set(context.loader().lootNumberManager().serialize(input.max, context));
            }
            return node;
        }
    };

    /**
     * Limits the provided value to between the minimum and maximum.<br>
     * This API currently guarantees that, if the minimum ends up being larger than the maximum, the resulting value
     * will be equal to the maximum.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to constrain to between the minimum and maximum
     * @return the constrained number
     */
    public long limit(@NotNull LootGenerationContext context, long number) {
        if (this.min != null) {
            number = Math.max(this.min.getLong(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getLong(context), number);
        }
        return number;
    }

    /**
     * Limits the provided value to between the minimum and maximum.<br>
     * This API currently guarantees that, if the minimum ends up being larger than the maximum, the resulting value
     * will be equal to the maximum.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to constrain to between the minimum and maximum
     * @return the constrained number
     */
    public double limit(@NotNull LootGenerationContext context, double number) {
        if (this.min != null) {
            number = Math.max(this.min.getDouble(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getDouble(context), number);
        }
        return number;
    }

    /**
     * Assures that the provided number is not smaller than the minimum and is not larger than the maximum. If either of
     * the bounds is null, it's always considered as passing.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to check the validity of
     * @return true if the provided number fits within {@link #min()} and {@link #max()}, and false otherwise
     */
    public boolean check(@NotNull LootGenerationContext context, long number) {
        return (this.min == null || this.min.getLong(context) <= number) &&
                (this.max == null || this.max.getLong(context) >= number);
    }

    /**
     * Assures that the provided number is not smaller than the minimum and is not larger than the maximum. If either of
     * the bounds is null, it's always considered as passing.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to check the validity of
     * @return true if the provided number fits within {@link #min()} and {@link #max()}, and false otherwise
     */
    public boolean check(@NotNull LootGenerationContext context, double number) {
        return (this.min == null || this.min.getDouble(context) <= number) &&
                (this.max == null || this.max.getDouble(context) >= number);
    }

}
