package net.goldenstack.loot;

import net.goldenstack.loot.util.Template;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot pool.
 * @param rolls the default number of rolls to occur
 * @param bonusRolls the number of bonus rolls to occur. Multiplied by the context's luck.
 * @param entries the entries to generate loot from.
 * @param predicates the predicates for loot generation
 * @param functions the modifiers applied to each item
 */
public record LootPool(@NotNull LootNumber rolls,
                       @NotNull LootNumber bonusRolls,
                       @NotNull List<LootEntry> entries,
                       @NotNull List<LootPredicate> predicates,
                       @NotNull List<LootFunction> functions) implements LootGenerator {

    @SuppressWarnings("UnstableApiUsage")
    public static final @NotNull BinaryTagSerializer<LootPool> SERIALIZER = Template.template(
            "rolls", LootNumber.SERIALIZER, LootPool::rolls,
            "bonus_rolls", LootNumber.SERIALIZER, LootPool::bonusRolls,
            "entries", LootEntry.SERIALIZER.list(), LootPool::entries,
            "conditions", LootPredicate.SERIALIZER.list(), LootPool::predicates,
            "functions", LootFunction.SERIALIZER.list(), LootPool::functions,
            LootPool::new
    );

    @Override
    public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
        if (!(LootPredicate.all(predicates, context))) return List.of();

        int rolls = this.rolls.getInt(context);

        Double luck = context.get(LootContext.LUCK);
        if (luck != null) {
            rolls += (int) Math.floor(luck * this.bonusRolls.getDouble(context));
        }

        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < rolls; i++) {
            LootEntry.Choice choice = pickChoice(entries, context);
            if (choice == null) continue;

            items.addAll(choice.apply(context));
        }

        return LootFunction.apply(functions, items, context);
    }
    
    /**
     * Picks a random choice from the choices generated by the provided entries, weighted with each choice's weight. If
     * no choices were generated, null is returned.
     * @param entries the entries to generate choices to choose from
     * @param context the context, to use if needed
     * @return the picked choice, or null if no choices were generated
     */
    static @Nullable LootEntry.Choice pickChoice(@NotNull List<LootEntry> entries, @NotNull LootContext context) {
        List<LootEntry.Choice> choices = new ArrayList<>();
        for (LootEntry entry : entries) {
            choices.addAll(entry.requestChoices(context));
        }

        if (choices.isEmpty()) {
            return null;
        }

        long totalWeight = 0;
        long[] weightMilestones = new long[choices.size()];
        for (int i = 0; i < choices.size(); i++) {
            // Prevent the weight of this choice from being less than 1
            totalWeight += Math.max(1, choices.get(i).getWeight(context));

            weightMilestones[i] = totalWeight;
        }

        long value = context.require(LootContext.RANDOM).nextLong(0, totalWeight);

        LootEntry.Choice choice = choices.getLast();

        for (int i = 0; i < weightMilestones.length; i++) {
            if (value < weightMilestones[i]) {
                choice = choices.get(i);
                break;
            }
        }

        return choice;
    }
}
