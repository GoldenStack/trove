package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * A condition that inverts the result of the child condition.
 * @param original the condition to invert
 */
public record InvertedCondition(@NotNull LootCondition original) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:inverted";

    /**
     * A standard map-based serializer for inverted conditions.
     */
    public static final @NotNull TypeSerializer<InvertedCondition> SERIALIZER =
            serializer(InvertedCondition.class,
                    field(LootCondition.class).name("original").nodePath("term")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        return !original.verify(context);
    }
}
