package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

/**
 * A modifier that limits the count of an item.
 * @param conditions the conditions required for use
 * @param limit the item count limiter
 */
public record LimitCountModifier(@NotNull List<LootCondition> conditions,
                                 @NotNull LootNumberRange limit) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:limit_count";

    /**
     * A standard map-based converter for limit count modifiers.
     */
    public static final @NotNull TypedLootConverter<LimitCountModifier> CONVERTER =
            converter(LimitCountModifier.class,
                    typeList(LootCondition.class).name("conditions").withDefault(List::of),
                    type(LootNumberRange.class).name("limit")
            );

    @Override
    public @Nullable Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var rule = StackingRule.get();

        var amount = rule.getAmount(input);
        var newAmount = (int) limit.limit(context, amount);

        if (amount == newAmount) {
            return input;
        }

        // Only apply if the rule allows it
        if (rule.canApply(input, newAmount)) {
            return rule.apply(input, newAmount);
        }

        return null;
    }

}
