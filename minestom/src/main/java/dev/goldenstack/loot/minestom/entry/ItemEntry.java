package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * An entry that always returns an item of its material.
 * @param itemType the material that is used for items
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record ItemEntry(@NotNull Material itemType,
                        long weight, long quality,
                        @NotNull List<LootModifier> modifiers,
                        @NotNull List<LootCondition> conditions) implements StandardSingleChoice {

    /**
     * A standard map-based converter for item entries.
     */
    public static final @NotNull KeyedLootConverter<ItemEntry> CONVERTER =
            converter(ItemEntry.class,
                    material().name("itemType").nodePath("name"),
                    implicit(long.class).name("weight").withDefault(1L),
                    implicit(long.class).name("quality").withDefault(0L),
                    modifier().list().name("modifiers").nodePath("functions").withDefault(List::of),
                    condition().list().name("conditions").withDefault(List::of)
            ).keyed("minecraft:item");

    public ItemEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean shouldGenerate(@NotNull LootGenerationContext context) {
        return LootCondition.all(conditions(), context);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootGenerationContext context) {
        return LootModifier.applyAll(modifiers(), LootBatch.of(ItemStack.of(itemType)), context);
    }
}