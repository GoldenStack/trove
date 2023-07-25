package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.FieldTypes.condition;

/**
 * Returns true if all of the {@link #conditions()} return true.
 * @param conditions the conditions that are tested
 */
public record AndCondition(@NotNull List<LootCondition> conditions) implements LootCondition {

    /**
     * A standard map-based converter for AND operator conditions.
     */
    public static final @NotNull KeyedLootConverter<AndCondition> CONVERTER =
            converter(AndCondition.class,
                    condition().list().name("conditions").nodePath("terms").withDefault(List::of)
            ).keyed("minecraft:all_of");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return LootCondition.all(conditions(), context);
    }
}
