package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

/**
 * A modifier that changes the count of any items provided to it.
 * @param conditions the conditions required for use
 * @param count the count to apply to stacks
 * @param add whether or not to add the previous item count to the new value
 */
public record SetCountModifier(@NotNull List<LootCondition> conditions,
                               @NotNull LootNumber count, boolean add) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:set_count";

    /**
     * A standard map-based converter for count set modifiers.
     */
    public static final @NotNull TypedLootConverter<SetCountModifier> CONVERTER =
            converter(SetCountModifier.class,
                    typeList(LootCondition.class).name("conditions").withDefault(List::of),
                    type(LootNumber.class).name("count"),
                    type(boolean.class).name("add")
            );

    @Override
    public @Nullable Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var rule = StackingRule.get();
        int newCount = (add ? rule.getAmount(input) : 0) + (int) this.count.getLong(context);
        if (!rule.canApply(input, newCount)) {
            // Clamp value if invalid, but exit if it's still invalid
            newCount = Math.max(Math.min(newCount, rule.getMaxSize(input)), 0);
            if (!rule.canApply(input, newCount)) {
                return null;
            }
        }
        return rule.apply(input, newCount);
    }

}
