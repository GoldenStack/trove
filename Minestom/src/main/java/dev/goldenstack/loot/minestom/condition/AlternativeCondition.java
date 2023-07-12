package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;

/**
 * Returns true if at least one of the {@link #conditions()} returns true.
 * @param conditions the conditions that are tested
 */
public record AlternativeCondition(@NotNull List<LootCondition> conditions) implements LootCondition {

    /**
     * A standard map-based converter for alternative conditions.
     */
    public static final @NotNull KeyedLootConverter<AlternativeCondition> CONVERTER =
            converter(AlternativeCondition.class,
                    condition().list().name("conditions").withDefault(List::of)
            ).keyed("minecraft:alternative");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return LootCondition.or(conditions(), context);
    }
}
