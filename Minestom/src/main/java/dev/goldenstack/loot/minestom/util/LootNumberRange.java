package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Represents a range between two loot numbers.
 * @param min the minimum value (optional)
 * @param max the maximum value (optional)
 */
public record LootNumberRange(@Nullable LootNumber<ItemStack> min, @Nullable LootNumber<ItemStack> max) {

    /**
     * @param context the context to feed to the min and max
     * @param number the number to limit
     * @return the provided number, but constrained so that it is between {@link #min()} and {@link #max()}
     */
    public long limit(@NotNull LootContext context, long number) {
        if (this.min != null) {
            number = Math.max(this.min.getLong(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getLong(context), number);
        }
        return number;
    }

    /**
     * @param context the context to feed to the min and max
     * @param number the number to limit
     * @return the provided number, but constrained so that it is between {@link #min()} and {@link #max()}
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
     * @param context the context to feed to the min and max
     * @param number the number to check
     * @return true if the provided number was within the range of {@link #min()} and {@link #max()}
     */
    public boolean check(@NotNull LootContext context, long number) {
        return (this.min == null || this.min.getLong(context) <= number) && (this.max == null || this.max.getLong(context) >= number);
    }

    /**
     * @param context the context to feed to the min and max
     * @param number the number to check
     * @return true if the provided number was within the range of {@link #min()} and {@link #max()}
     */
    public boolean check(@NotNull LootContext context, double number) {
        return (this.min == null || this.min.getDouble(context) <= number) && (this.max == null || this.max.getDouble(context) >= number);
    }

    /**
     * @param range the range to attempt to serialize
     * @param context the context, to use for serializing the minimum and maximum
     * @return the JSON element that was formed
     * @throws ConfigurateException if either the minimum or maximum could not be serialized
     */
    public static @NotNull ConfigurationNode serialize(@NotNull LootNumberRange range, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        ConfigurationNode node = context.loader().createNode();
        if (range.min != null) {
            node.node("min").set(context.loader().lootNumberManager().serialize(range.min, context));
        }
        if (range.max != null) {
            node.node("max").set(context.loader().lootNumberManager().serialize(range.max, context));
        }
        return node;
    }

    /**
     * @param node the node to attempt to deserialize
     * @param context the context, to use for deserializing the minimum and maximum
     * @return the range that was created
     * @throws ConfigurateException if the element was not a valid loot number range
     */
    public static @NotNull LootNumberRange deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        if (node.empty()) {
            return new LootNumberRange(null, null);
        }
        return new LootNumberRange(
                node.hasChild("min") ? context.loader().lootNumberManager().deserialize(node.node("min"), context) : null,
                node.hasChild("max") ? context.loader().lootNumberManager().deserialize(node.node("max"), context) : null
        );
    }
}
