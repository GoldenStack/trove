package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot entry that will return the combined results of all of its children.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met
 */
public record GroupEntry(@NotNull List<LootEntry<ItemStack>> children, @NotNull List<LootCondition<ItemStack>> conditions) implements LootEntry<ItemStack> {

    /**
     * A standard map-based converter for group entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, GroupEntry> CONVERTER = Utils.createKeyedConverter("minecraft:group", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("children").set(Utils.serializeList(input.children(), context.loader().lootEntryManager()::serialize, context));
                result.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
            }, (input, context) -> new GroupEntry(
                    Utils.deserializeList(input.node("children"), context.loader().lootEntryManager()::deserialize, context),
                    Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context)
            ));

    public GroupEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Choice<ItemStack>> requestChoices(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        List<Choice<ItemStack>> choices = new ArrayList<>();
        for (var entry : this.children()) {
            choices.addAll(entry.requestChoices(context));
        }
        return choices;
    }

}
