package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

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

    public static final @NotNull String KEY = "minecraft:looting_enchant";

    /**
     * A standard map-based serializer for apply looting modifiers.
     */
    public static final @NotNull TypeSerializer<ApplyLootingModifier> SERIALIZER =
            serializer(ApplyLootingModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(LootNumber.class).name("lootingMultiplier").nodePath("count"),
                    field(int.class).name("limiter").nodePath("limit").fallback(0)
            );

    @Override
    public @NotNull Object modifyTyped(@NotNull ItemStack input, @NotNull LootContext context) {
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
