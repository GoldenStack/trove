package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A modifier that limits the count of an item.
 * @param conditions the conditions required for use
 * @param limit the item count limiter
 */
public record LimitCountModifier(@NotNull List<LootCondition> conditions,
                                 @NotNull LootNumberRange limit) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:limit_count";

    /**
     * A standard map-based serializer for limit count modifiers.
     */
    public static final @NotNull TypeSerializer<LimitCountModifier> SERIALIZER =
            serializer(LimitCountModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(LootNumberRange.class).name("limit")
            );

    @Override
    public @NotNull Object modifyTyped(@NotNull ItemStack input, @NotNull LootContext context) {
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
        return rule.canApply(input, newAmount) ? rule.apply(input, newAmount) : input;
    }

}
