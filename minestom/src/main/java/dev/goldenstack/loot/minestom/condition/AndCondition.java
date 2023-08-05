package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Returns true if all of the {@link #conditions()} return true.
 * @param conditions the conditions that are tested
 */
public record AndCondition(@NotNull List<LootCondition> conditions) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:all_of";

    /**
     * A standard map-based serializer for AND operator conditions.
     */
    public static final @NotNull TypeSerializer<AndCondition> SERIALIZER =
            serializer(AndCondition.class,
                    field(LootCondition.class).name("conditions").nodePath("terms").as(list()).fallback(List::of)
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return LootCondition.all(conditions(), context);
    }
}
