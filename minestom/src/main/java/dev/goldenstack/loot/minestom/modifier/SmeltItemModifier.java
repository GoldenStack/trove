package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * Smelts items provided to it, removing them if they couldn't be smelted.
 * @param conditions the conditions required for smelting
 */
public record SmeltItemModifier(@NotNull List<LootCondition> conditions) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:furnace_smelt";

    /**
     * A standard map-based converter for item smelting modifiers.
     */
    public static final @NotNull TypedLootConverter<SmeltItemModifier> CONVERTER =
            converter(SmeltItemModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
            );

    @Override
    public @Nullable Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var rule = StackingRule.get();

        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);
        ItemStack smelted = vanilla.smeltItem(input);

        // Fail if the item couldn't be smelted or if the new item can't have the same count applied
        if (smelted == null || !rule.canApply(smelted, input.amount())) {
            return null;
        }

        return rule.apply(smelted, input.amount());
    }

}
