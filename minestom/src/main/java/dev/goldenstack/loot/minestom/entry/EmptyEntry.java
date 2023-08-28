package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * An entry that always returns an empty list of items.
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record EmptyEntry(long weight, long quality,
                         @NotNull List<LootModifier> modifiers,
                         @NotNull List<LootCondition> conditions) implements StandardSingleChoice {

    public static final @NotNull String KEY = "minecraft:empty";

    /**
     * A standard map-based serializer for empty entries.
     */
    public static final @NotNull TypeSerializer<EmptyEntry> SERIALIZER =
            serializer(EmptyEntry.class,
                    field(long.class).name("weight").fallback(1L),
                    field(long.class).name("quality").fallback(0L),
                    field(LootModifier.class).name("modifiers").nodePath("functions").as(list()).fallback(List::of),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
            );

    public EmptyEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean shouldGenerate(@NotNull LootContext context) {
        return LootCondition.all(conditions(), context);
    }

    @Override
    public @NotNull List<Object> generate(@NotNull LootContext context) {
        return List.of();
    }
}
