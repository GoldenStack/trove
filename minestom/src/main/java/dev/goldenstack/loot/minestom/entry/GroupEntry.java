package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A loot entry that will return the combined results of all of its children.
 * @param children the child entries to test
 * @param conditions the conditions that all must be met
 */
public record GroupEntry(@NotNull List<LootEntry> children, @NotNull List<LootCondition> conditions) implements LootEntry {

    public static final @NotNull String KEY = "minecraft:group";

    /**
     * A standard map-based serializer for group entries.
     */
    public static final @NotNull TypeSerializer<GroupEntry> SERIALIZER =
            serializer(GroupEntry.class,
                    field(LootEntry.class).name("children").as(list()),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
            );

    public GroupEntry {
        children = List.copyOf(children);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return List.of();
        }
        List<Choice> choices = new ArrayList<>();
        for (var entry : this.children()) {
            choices.addAll(entry.requestChoices(context));
        }
        return choices;
    }

}
