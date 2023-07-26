package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * A modifier that enchants items with a specific level.
 * @param conditions the conditions required for use
 * @param levelRange the enchantment level that will be applied to the item
 * @param permitTreasure whether or not treasure enchantments can possibly be applied on the item
 */
public record LevelledEnchantModifier(@NotNull List<LootCondition> conditions,
                                      @NotNull LootNumber levelRange,
                                      boolean permitTreasure) implements ItemStackModifier {

    /**
     * A standard map-based converter for enchant-with-levels modifiers.
     */
    public static final @NotNull KeyedLootConverter<LevelledEnchantModifier> CONVERTER =
            converter(LevelledEnchantModifier.class,
                    condition().list().name("conditions").withDefault(List::of),
                    number().name("levelRange").nodePath("levels"),
                    implicit(boolean.class).name("permitTreasure").nodePath("treasureEnchantmentsAllowed").withDefault(false)
            ).keyed("minecraft:enchant_with_levels");

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);
        return vanilla.enchantItem(context.random(), input, (int) levelRange.getLong(context), permitTreasure);
    }

}
