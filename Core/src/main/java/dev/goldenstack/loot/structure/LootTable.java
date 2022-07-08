package dev.goldenstack.loot.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootAware;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.util.JsonUtils;
import dev.goldenstack.loot.util.LootModifierHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot table stores a list of loot pools, loot modifiers, and required criterion.
 * @param pools the pools that will be asked for loot
 * @param modifiers modifiers to apply to each and every loot item that is generated from the pools
 * @param <L> the loot item
 */
public record LootTable<L>(@NotNull List<LootPool<L>> pools,
                           @NotNull List<LootModifier<L>> modifiers) implements LootAware<L>, LootModifierHolder<L> {

    /**
     * Handles serialization for loot tables - it's not final so that custom implementations are possible
     * @param <L> the loot item
     */
    public static class Converter<L> {

        /**
         * Override this method to provide a custom implementation.
         * @param element the element to attempt to deserialize
         * @param context the context, to use if required
         * @return the loot table that was created,
         * @throws LootConversionException if the element, in any way, could not be converted to a valid loot table
         */
        public @NotNull LootTable<L> deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<L> context) throws LootConversionException {
            JsonObject object = JsonUtils.assureJsonObject(element, null);
            return new LootTable<>(
                    JsonUtils.deserializeJsonArray(JsonUtils.assureJsonArray(object.get("pools"), "pools"), "pools", (e, k) -> context.loader().lootPoolConverter().deserialize(e, context)),
                    object.has("modifiers") ? context.loader().lootModifierManager().deserializeList(JsonUtils.assureJsonArray(object.get("modifiers"), "modifiers"), context) : List.of()
            );
        }

        /**
         * Override this method to provide a custom implementation.
         * @param input the loot table to attempt to serialize
         * @param context the context, to use if required
         * @return the successfully serialized loot table
         * @throws LootConversionException if something in the provided loot table could not be serialized
         */
        public @NotNull JsonElement serialize(@NotNull LootTable<L> input, @NotNull LootConversionContext<L> context) throws LootConversionException {
            JsonObject object = new JsonObject();
            object.add("entries", JsonUtils.serializeJsonArray(input.pools(), pool -> context.loader().lootPoolConverter().serialize(pool, context)));
            if (!input.modifiers().isEmpty()) {
                object.add("modifiers", context.loader().lootModifierManager().serializeList(input.modifiers(), context));
            }
            return object;
        }

    }

    public LootTable {
        pools = List.copyOf(pools);
        modifiers = List.copyOf(modifiers);
    }

    /**
     * Generates a list of loot items. This is pretty simple: it just gets the loot from each pool and then applies
     * every modifier to it.<br>
     * Note: The returned list may or may not be immutable.<br>
     * @param context the context to use for generation
     * @return every loot item that was generated
     */
    public @NotNull List<L> generate(@NotNull LootContext context) {
        // No loot can be generated
        if (this.pools.isEmpty()) {
            return List.of();
        }

        List<L> items = new ArrayList<>();
        for (LootPool<L> pool : pools) {
            for (L lootItem : pool.generate(context)) {
                items.add(modify(lootItem, context));
            }
        }
        return items;
    }
}
