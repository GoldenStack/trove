package net.goldenstack.loot.util;

import net.goldenstack.loot.LootContext;
import net.goldenstack.loot.LootNumber;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An inclusive number range based on loot numbers.
 * @param min the optional minimum value
 * @param max the optional maximum value
 */
public record LootNumberRange(@Nullable LootNumber min, @Nullable LootNumber max) {

    @SuppressWarnings({"DataFlowIssue", "UnstableApiUsage"})
    public static final @NotNull BinaryTagSerializer<LootNumberRange> SERIALIZER = Template.compoundSplit(
            Serial.DOUBLE.map(d -> new LootNumberRange(new LootNumber.Constant(d), new LootNumber.Constant(d)), null)
                    .optional(new LootNumberRange(null, null)),
            Template.template(
                    "min", LootNumber.SERIALIZER.optional(), LootNumberRange::min,
                    "max", LootNumber.SERIALIZER.optional(), LootNumberRange::max,
                    LootNumberRange::new
            ).optional(new LootNumberRange(null, null))
    );

    /**
     * Limits the provided value to between the minimum and maximum.<br>
     * This API currently guarantees that, if the minimum ends up being larger than the maximum, the resulting value
     * will be equal to the maximum.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to constrain to between the minimum and maximum
     * @return the constrained number
     */
    public long limit(@NotNull LootContext context, long number) {
        if (this.min != null) {
            number = Math.max(this.min.getInt(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getInt(context), number);
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
    public double limit(@NotNull LootContext context, double number) {
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
    public boolean check(@NotNull LootContext context, long number) {
        return (this.min == null || this.min.getInt(context) <= number) &&
                (this.max == null || this.max.getInt(context) >= number);
    }

    /**
     * Assures that the provided number is not smaller than the minimum and is not larger than the maximum. If either of
     * the bounds is null, it's always considered as passing.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to check the validity of
     * @return true if the provided number fits within {@link #min()} and {@link #max()}, and false otherwise
     */
    public boolean check(@NotNull LootContext context, double number) {
        return (this.min == null || this.min.getDouble(context) <= number) &&
                (this.max == null || this.max.getDouble(context) >= number);
    }

}
