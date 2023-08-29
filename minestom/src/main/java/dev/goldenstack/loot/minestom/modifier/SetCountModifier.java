package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

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
     * A standard map-based serializer for count set modifiers.
     */
    public static final @NotNull TypeSerializer<SetCountModifier> SERIALIZER =
            serializer(SetCountModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(LootNumber.class).name("count"),
                    field(boolean.class).name("add")
            );

    @Override
    public @NotNull Object modifyTyped(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var rule = StackingRule.get();
        int newCount = (add ? rule.getAmount(input) : 0) + (int) this.count.getLong(context);
        if (!rule.canApply(input, newCount)) {
            // Clamp value if invalid, but exit if it's still invalid
            newCount = Math.max(Math.min(newCount, rule.getMaxSize(input)), 0);
            if (!rule.canApply(input, newCount)) {
                return input;
            }
        }
        return rule.apply(input, newCount);
    }

}
