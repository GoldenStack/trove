package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.typeList;

/**
 * A loot entry that will return the results of the first child that returns any results.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record AlternativeEntry(@NotNull List<LootEntry> children, @NotNull List<LootCondition> conditions) implements LootEntry {

    public static final @NotNull String KEY = "minecraft:alternatives";

    /**
     * A standard map-based converter for alternative entries.
     */
    public static final @NotNull TypedLootConverter<AlternativeEntry> CONVERTER =
            converter(AlternativeEntry.class,
                    typeList(LootEntry.class).name("children"),
                    typeList(LootCondition.class).name("conditions").withDefault(List::of)
            );

    public AlternativeEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
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
