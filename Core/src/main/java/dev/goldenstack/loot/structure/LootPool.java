package dev.goldenstack.loot.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootAware;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.util.JsonUtils;
import dev.goldenstack.loot.util.LootModifierHolder;
import dev.goldenstack.loot.util.LootRequirementHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot pool is a collection of {@link #entries()}, guarded by a list of {@link #requirements()} and modified by a
 * list of {@link #modifiers()}, that gets run a selected number of times based on {@link #rolls()}.
 * @param <L> the loot item
 */
public record LootPool<L>(@NotNull List<LootEntry<L>> entries,
                          @NotNull List<LootRequirement<L>> requirements,
                          @NotNull List<LootModifier<L>> modifiers,
                          @NotNull LootNumber<L> rolls) implements LootRequirementHolder<L>, LootModifierHolder<L>, LootAware<L> {

    /**
     * Handles serialization for loot pools - it's not final so that custom implementations are possible
     * @param <L> the loot item
     */
    public static class Converter<L> {

        /**
         * Override this method to provide a custom implementation.
         * @param element the element to attempt to deserialize
         * @param context the context, to use if required
         * @return the loot pool that was created,
         * @throws LootConversionException if the element, in any way, could not be converted to a valid loot pool
         */
        public @NotNull LootPool<L> deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<L> context) throws LootConversionException {
            JsonObject object = JsonUtils.assureJsonObject(element, null);
            return new LootPool<>(
                    context.loader().lootEntryManager().deserializeList(JsonUtils.assureJsonArray(object.get("entries"), "entries"), context),
                    object.has("requirements") ? context.loader().lootRequirementManager().deserializeList(JsonUtils.assureJsonArray(object.get("requirements"), "requirements"), context) : List.of(),
                    object.has("modifiers") ? context.loader().lootModifierManager().deserializeList(JsonUtils.assureJsonArray(object.get("modifiers"), "modifiers"), context) : List.of(),
                    context.loader().lootNumberManager().deserialize(object.get("rolls"), context)
            );
        }

        /**
         * Override this method to provide a custom implementation.
         * @param input the loot pool to attempt to serialize
         * @param context the context, to use if required
         * @return the successfully serialized loot pool
         * @throws LootConversionException if something in the provided loot pool could not be serialized
         */
        public @NotNull JsonElement serialize(@NotNull LootPool<L> input, @NotNull LootConversionContext<L> context) throws LootConversionException {
            JsonObject object = new JsonObject();
            object.add("entries", context.loader().lootEntryManager().serializeList(input.entries(), context));
            if (!input.requirements().isEmpty()) {
                object.add("requirements", context.loader().lootRequirementManager().serializeList(input.requirements(), context));
            }
            if (!input.modifiers().isEmpty()) {
                object.add("modifiers", context.loader().lootModifierManager().serializeList(input.modifiers(), context));
            }
            object.add("rolls", context.loader().lootNumberManager().serialize(input.rolls(), context));
            return object;
        }
    }

    public LootPool {
        entries = List.copyOf(entries);
        requirements = List.copyOf(requirements);
        modifiers = List.copyOf(modifiers);
    }

    /**
     * Generates a list of loot items based on this pool's information.<br>
     * Note: The returned list may or may not be immutable.<br>
     * The steps of loot generation are as follows:
     * <ul>
     *     <li>If this pool's requirements are not all fulfilled, return an empty list.</li>
     *     <li>Generate a number of rolls from {@link #rolls()}</li>
     *     <li>For each roll, collect a list of the combined options provided by each entry via
     *     {@link LootEntry#requestOptions(LootContext)}, pick a random option from the list based on its weight, and
     *     then add its items (via {@link LootEntry.Option#generate(LootContext)} to the list of items.</li>
     *     <li>Apply each modifier from {@link #modifiers()} to each item</li>
     * </ul>
     * @param context context to be used for generation
     * @return the full list of generated loot items
     */
    public @NotNull List<L> generate(@NotNull LootContext context) {
        if (!passes(context)) {
            return List.of();
        }

        List<L> items = new ArrayList<>();
        long rolls = this.rolls.getLong(context);

        for (int i = 0; i < rolls; i++) {
            // Weight and choices must be recalculated each time as their results theoretically may change
            List<LootEntry.Option<L>> options = new ArrayList<>();
            for (LootEntry<L> entry : this.entries) {
                options.addAll(entry.requestOptions(context));
            }

            if (options.isEmpty()) {
                continue;
            }

            long totalWeight = 0;
            long[] lowerWeightMilestones = new long[options.size()];
            for (int j = 0; j < options.size(); j++) {
                lowerWeightMilestones[j] = totalWeight;
                // Prevent the weight from being less than 1
                totalWeight += Math.max(1, options.get(j).getWeight(context));
            }

            long value = context.random().nextLong(0, totalWeight);

            LootEntry.Option<L> option = options.get(options.size() - 1); // Just set it to the last one (instead of throwing an exception) in case none somehow pass

            for (int j = 0; j < lowerWeightMilestones.length; j++) {
                if (value >= lowerWeightMilestones[j]) {
                    option = options.get(j);
                    break;
                }
            }

            items.addAll(option.generate(context));
        }

        items.replaceAll(lootItem -> modify(lootItem, context));
        return items;
    }

}
