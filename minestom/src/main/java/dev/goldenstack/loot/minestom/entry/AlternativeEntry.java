package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A loot entry that will return the results of the first child that returns any results.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record AlternativeEntry(@NotNull List<LootEntry> children, @NotNull List<LootCondition> conditions) implements LootEntry {

    public static final @NotNull String KEY = "minecraft:alternatives";

    /**
     * A standard map-based serializer for alternative entries.
     */
    public static final @NotNull TypeSerializer<AlternativeEntry> SERIALIZER =
            serializer(AlternativeEntry.class,
                    field(LootEntry.class).name("children").as(list()),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
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
