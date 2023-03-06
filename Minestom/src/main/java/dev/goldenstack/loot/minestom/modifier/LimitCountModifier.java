package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.numberRange;

/**
 * A modifier that limits the count of an item.
 * @param conditions the conditions required for use
 * @param limit the item count limiter
 */
public record LimitCountModifier(@NotNull List<LootCondition> conditions,
                                 @NotNull LootNumberRange limit) implements LootModifier.Filtered<ItemStack> {

    /**
     * A standard map-based converter for limit count modifiers.
     */
    public static final @NotNull KeyedLootConverter<LimitCountModifier> CONVERTER =
            converter(LimitCountModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    numberRange().name("limit")
            ).keyed("minecraft:limit_count");

    @Override
    public @Nullable Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
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

    @Override
    public @NotNull Type filteredType() {
        return ItemStack.class;
    }
}
