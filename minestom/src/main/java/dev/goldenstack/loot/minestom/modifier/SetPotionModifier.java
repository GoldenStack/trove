package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Sets the potion effect attached to each provided item.
 * @param conditions the conditions required for use
 * @param potion the potion to set
 */
public record SetPotionModifier(@NotNull List<LootCondition> conditions, @NotNull PotionType potion) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:set_potion";

    /**
     * A standard map-based serializer for set potion modifiers.
     */
    public static final @NotNull TypeSerializer<SetPotionModifier> SERIALIZER =
            serializer(SetPotionModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(PotionType.class).name("potion").nodePath("id")
            );

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
