package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.FieldTypes.condition;

/**
 * Smelts items provided to it, removing them if they couldn't be smelted.
 * @param conditions the conditions required for smelting
 */
public record SmeltItemModifier(@NotNull List<LootCondition> conditions) implements ItemStackModifier {

    /**
     * A standard map-based converter for item smelting modifiers.
     */
    public static final @NotNull KeyedLootConverter<SmeltItemModifier> CONVERTER =
            converter(SmeltItemModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new)
            ).keyed("minecraft:furnace_smelt");

    @Override
    public @Nullable Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
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
