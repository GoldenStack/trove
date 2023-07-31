package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

/**
 * Sets the damage of each provided item. If {@link #add()} is false, the item's durability is set to
 * {@link #modifiedDurability()} scaled by the item's maximum durability, but if {@link #add()} is true the modified
 * durability, scaled by the item's maximum durability, is added to its current durability.
 * @param conditions the conditions required for use
 * @param modifiedDurability the portion of the item's durability (0 to 1) to be added/set
 * @param add whether or not the item's current durability should be added to the {@link #modifiedDurability()}
 */
public record SetDamageModifier(@NotNull List<LootCondition> conditions,
                                @NotNull LootNumber modifiedDurability, boolean add) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:set_damage";

    /**
     * A standard map-based converter for set damage modifiers.
     */
    public static final @NotNull TypedLootConverter<SetDamageModifier> CONVERTER =
            converter(SetDamageModifier.class,
                    typeList(LootCondition.class).name("conditions").withDefault(List::of),
                    type(LootNumber.class).name("modifiedDurability").nodePath("damage"),
                    type(boolean.class).name("add").withDefault(false)
            );

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        final int maxDamage = input.material().registry().maxDamage();

        // Don't change damage if it has no durability
        if (maxDamage == 0) {
            return input;
        }

        double currentDurability = add ? 1 - (input.meta().getDamage() / (double) maxDamage) : 0;

        double newDurability = Math.min(Math.max(currentDurability + modifiedDurability.getDouble(context), 0), 1);

        return input.withMeta(meta -> meta.damage((int) Math.floor((1 - newDurability) * maxDamage)));
    }
}