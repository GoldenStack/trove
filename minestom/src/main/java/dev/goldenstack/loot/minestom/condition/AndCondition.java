package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * Returns true if all of the {@link #conditions()} return true.
 * @param conditions the conditions that are tested
 */
public record AndCondition(@NotNull List<LootCondition> conditions) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:all_of";

    /**
     * A standard map-based converter for AND operator conditions.
     */
    public static final @NotNull TypedLootConverter<AndCondition> CONVERTER =
            converter(AndCondition.class,
                    field(LootCondition.class).name("conditions").nodePath("terms").as(list()).fallback(List::of)
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return LootCondition.all(conditions(), context);
    }
}
