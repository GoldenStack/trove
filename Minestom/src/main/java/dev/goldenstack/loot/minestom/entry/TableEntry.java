package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.generation.LootTable;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that is linked to a loot table using {@link LootContextKeys#REGISTERED_TABLES} and {@link #tableIdentifier()}.
 * @param tableIdentifier the identifier to look for tables with
 * @param weight the base weight of this entry - see {@link StandardWeightedOption#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedOption#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record TableEntry(@NotNull String tableIdentifier,
                        long weight, long quality,
                        @NotNull List<LootModifier<ItemStack>> modifiers,
                        @NotNull List<LootCondition<ItemStack>> conditions) implements SingleOptionEntry<ItemStack>, StandardWeightedOption<ItemStack> {

    /**
     * A standard map-based converter for table entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, TableEntry> CONVERTER = Utils.createKeyedConverter("minecraft:loot_table", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("name").set(input.tableIdentifier);
                result.node("weight").set(input.weight);
                result.node("quality").set(input.quality);
                result.node("functions").set(Utils.serializeList(input.modifiers(), context.loader().lootModifierManager()::serialize, context));
                result.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
            }, (input, context) -> new TableEntry(
                    input.node("name").require(String.class),
                    input.node("weight").getLong(1),
                    input.node("quality").getLong(0),
                    Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context),
                    Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context)
            ));

    public TableEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        LootTable<ItemStack> table = context.assure(LootContextKeys.REGISTERED_TABLES).get(tableIdentifier);
        return table == null ? List.of() : LootModifier.applyAll(modifiers(), table.generate(context), context);
    }
}