package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot entry that will return the combined results of all of the entry's children until one of them returns nothing.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met
 */
public record SequenceEntry(@NotNull List<LootEntry<ItemStack>> children, @NotNull List<LootCondition<ItemStack>> conditions) implements LootEntry<ItemStack> {

    /**
     * A standard map-based converter for sequence entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, SequenceEntry> CONVERTER = new KeyedLootConverter<>("minecraft:sequence", TypeToken.get(SequenceEntry.class)) {
        @Override
        public void serialize(@NotNull SequenceEntry input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("children").set(Utils.serializeList(input.children(), context.loader().lootEntryManager()::serialize, context));
            result.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
        }

        @Override
        public @NotNull SequenceEntry deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new SequenceEntry(
                    Utils.deserializeList(input.node("children"), context.loader().lootEntryManager()::deserialize, context),
                    Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context)
            );
        }
    };

    public SequenceEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Option<ItemStack>> requestOptions(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        List<Option<ItemStack>> options = new ArrayList<>();
        for (var entry : this.children()) {
            var choices = entry.requestOptions(context);
            if (choices.isEmpty()) {
                break;
            }
            options.addAll(choices);
        }
        return options;
    }
}
