package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.FieldTypes.condition;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.potionType;

/**
 * Sets the potion effect attached to each provided item.
 * @param conditions the conditions required for use
 * @param potion the potion to set
 */
public record SetPotionModifier(@NotNull List<LootCondition> conditions, @NotNull PotionType potion) implements ItemStackModifier {

    /**
     * A standard map-based converter for set potion modifiers.
     */
    public static final @NotNull KeyedLootConverter<SetPotionModifier> CONVERTER =
            converter(SetPotionModifier.class,
                    condition().list().name("conditions").withDefault(List::of),
                    potionType().name("potion").nodePath("id")
            ).keyed("minecraft:set_potion");

    private static final @NotNull Tag<String> POTION_TAG = Tag.String("Potion");

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!(LootCondition.all(conditions(), context))) {
            return input;
        }

        if (potion.namespace().asString().equals("minecraft:empty")) {
            return input.withMeta(meta -> meta.removeTag(POTION_TAG));
        } else {
            return input.withTag(POTION_TAG, potion.namespace().asString());
        }
    }
}
