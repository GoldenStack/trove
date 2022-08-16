package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A loot entry that will return the results of the first child that returns any results.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record AlternativeEntry(@NotNull List<LootEntry<ItemStack>> children, @NotNull List<LootCondition<ItemStack>> conditions) implements LootEntry<ItemStack> {

    /**
     * A standard map-based converter for alternative entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, AlternativeEntry> CONVERTER = Utils.createKeyedConverter("minecraft:alternatives", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("children").set(Utils.serializeList(input.children(), context.loader().lootEntryManager()::serialize, context));
                result.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
            }, (input, context) -> new AlternativeEntry(
                    Utils.deserializeList(input.node("children"), context.loader().lootEntryManager()::deserialize, context),
                    Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context)
            ));

    public AlternativeEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Option<ItemStack>> requestOptions(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        for (var entry : this.children()) {
            var options = entry.requestOptions(context);
            if (!options.isEmpty()) {
                return options;
            }
        }
        return List.of();
    }

}
