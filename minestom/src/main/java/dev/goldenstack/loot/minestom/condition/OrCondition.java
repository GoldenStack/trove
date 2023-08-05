package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * Returns true if at least one of the {@link #conditions()} returns true.
 * @param conditions the conditions that are tested
 */
public record OrCondition(@NotNull List<LootCondition> conditions) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:any_of";

    /**
     * A standard map-based converter for OR operator conditions.
     */
    public static final @NotNull TypeSerializer<OrCondition> CONVERTER =
            converter(OrCondition.class,
                    field(LootCondition.class).name("conditions").nodePath("terms").as(list()).fallback(List::of)
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return LootCondition.any(conditions(), context);
    }
}
