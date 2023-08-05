package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * A modifier that enchants items with a specific level.
 * @param conditions the conditions required for use
 * @param levelRange the enchantment level that will be applied to the item
 * @param permitTreasure whether or not treasure enchantments can possibly be applied on the item
 */
public record LevelledEnchantModifier(@NotNull List<LootCondition> conditions,
                                      @NotNull LootNumber levelRange,
                                      boolean permitTreasure) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:enchant_with_levels";

    /**
     * A standard map-based converter for enchant-with-levels modifiers.
     */
    public static final @NotNull TypeSerializer<LevelledEnchantModifier> CONVERTER =
            converter(LevelledEnchantModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(LootNumber.class).name("levelRange").nodePath("levels"),
                    field(boolean.class).name("permitTreasure").nodePath("treasureEnchantmentsAllowed").fallback(false)
            );

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var vanilla = context.assure(LootContextKeys.VANILLA_INTERFACE);
        return vanilla.enchantItem(context.random(), input, (int) levelRange.getLong(context), permitTreasure);
    }

}
