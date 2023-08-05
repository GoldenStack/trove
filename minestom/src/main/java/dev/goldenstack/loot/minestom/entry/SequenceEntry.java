package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * A loot entry that will return the combined results of all of the entry's children until one of them returns nothing.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met
 */
public record SequenceEntry(@NotNull List<LootEntry> children, @NotNull List<LootCondition> conditions) implements LootEntry {

    public static final @NotNull String KEY = "minecraft:sequence";

    /**
     * A standard map-based converter for sequence entries.
     */
    public static final @NotNull TypeSerializer<SequenceEntry> CONVERTER =
            converter(SequenceEntry.class,
                    field(LootEntry.class).name("children").as(list()),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
            );

    public SequenceEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        List<Choice> options = new ArrayList<>();
        for (var entry : this.children()) {
            var choices = entry.requestChoices(context);
            if (choices.isEmpty()) {
                break;
            }
            options.addAll(choices);
        }
        return options;
    }
}
