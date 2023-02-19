package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.entry;

/**
 * A loot entry that will return the results of the first child that returns any results.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record AlternativeEntry(@NotNull List<LootEntry> children, @NotNull List<LootCondition> conditions) implements LootEntry {

    /**
     * A standard map-based converter for alternative entries.
     */
    public static final @NotNull KeyedLootConverter<AlternativeEntry> CONVERTER =
            converter(AlternativeEntry.class,
                    entry().list().name("children"),
                    condition().list().name("conditions").withDefault(ArrayList::new)
            ).keyed("minecraft:alternatives");

    public AlternativeEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Choice> requestChoices(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        for (var entry : this.children()) {
            var options = entry.requestChoices(context);
            if (!options.isEmpty()) {
                return options;
            }
        }
        return List.of();
    }

}
