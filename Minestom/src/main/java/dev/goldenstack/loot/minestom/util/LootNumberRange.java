package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

/**
 * An inclusive number range based on loot numbers.
 * @param min the optional minimum value
 * @param max the optional maximum value
 */
public record LootNumberRange(@Nullable LootNumber min, @Nullable LootNumber max) {

    /**
     * A standard converter for loot number ranges that deserializes any numerical scalars to a range consisting of two
     * equivalent {@link ConstantNumber} instances holding the number, but otherwise handles it normally with map-based
     * values.
     */
    public static final @NotNull AdditiveConverter<LootNumberRange> CONVERTER = Utils.createAdditive(
            (input, result, context) -> {
                if (input.min != null) {
                    context.loader().lootNumberManager().serialize(input.min, result.node("min"), context);
                }
                if (input.max != null) {
                    context.loader().lootNumberManager().serialize(input.max, result.node("min"), context);
                }
            },
            (input, context) -> {
                if (input.isNull()) {
                    return new LootNumberRange(null, null);
                } else if (input.isMap()) {
                    return new LootNumberRange(
                            input.hasChild("min") ? context.loader().lootNumberManager().deserialize(input.node("min"), context) : null,
                            input.hasChild("max") ? context.loader().lootNumberManager().deserialize(input.node("max"), context) : null
                    );
                } else { // Is either invalid or a number, so we can assume here
                    var number = input.get(Double.class);

                    if (number == null) {
                        throw new ConfigurateException(input, "Expected null, a map, or a scalar");
                    }
                    var constant = new ConstantNumber(number);
                    return new LootNumberRange(constant, constant);
                }
            }
    );

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
