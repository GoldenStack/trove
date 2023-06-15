package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.FieldTypes.*;

/**
 * A modifier that adds a certain amount to each item, where the amount added is the killer's looting multiplied by
 * {@link #lootingMultiplier()}.
 * @param conditions the conditions required for use
 * @param lootingMultiplier the number by which the added stack size (originally just the killer's looting) is
 *                          multiplied
 * @param limiter the maximum new value after the count is applied
 */
public record ApplyLootingModifier(@NotNull List<LootCondition> conditions,
                                   @NotNull LootNumber lootingMultiplier,
                                   int limiter) implements ItemStackModifier {

    /**
     * A standard map-based converter for apply looting modifiers.
     */
    public static final @NotNull KeyedLootConverter<ApplyLootingModifier> CONVERTER =
            converter(ApplyLootingModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    number().name("lootingMultiplier").nodePath("count"),
                    implicit(int.class).name("limiter").nodePath("limit").withDefault(0)
            ).keyed("minecraft:looting_enchant");

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        Entity killer = context.get(LootContextKeys.KILLER_ENTITY);
        if (killer == null || !LootCondition.all(conditions(), context)) {
            return input;
        }

        int looting = context.assure(LootContextKeys.VANILLA_INTERFACE).getLooting(killer);
        if (looting == 0) {
            return input;
        }

        int additionalAmount = (int) Math.round(looting * this.lootingMultiplier.getDouble(context));

        var rule = StackingRule.get();

        int newCount = rule.getAmount(input) + additionalAmount;

        // Limit the size to the maximum size or the provided limiter value
        int sizeLimit = rule.getMaxSize(input);
        if (limiter != 0 && limiter < sizeLimit) {
            sizeLimit = limiter;
        }

        newCount = Math.min(newCount, sizeLimit);

        // Apply the new count only if it is valid.
        if (rule.canApply(input, newCount)) {
            input = rule.apply(input, newCount);
        }

        return input;
    }
}
