package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.minestom.vanilla.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Verifies that the location at the context (i.e. the {@link LootContextKeys#WORLD} and {@link LootContextKeys#ORIGIN})
 * is valid according to the {@link #predicate()}.
 */
public record LocationCheckCondition(@NotNull VanillaInterface.LocationPredicate predicate,
                                     double xOffset, double yOffset, double zOffset) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:location_check";

    /**
     * A standard map-based serializer for location check conditions.
     */
    public static final @NotNull TypeSerializer<LocationCheckCondition> SERIALIZER =
            serializer(LocationCheckCondition.class,
                    field(VanillaInterface.LocationPredicate.class).name("predicate"),
                    field(double.class).name("xOffset").nodePath("offsetX").fallback(0d),
                    field(double.class).name("yOffset").nodePath("offsetY").fallback(0d),
                    field(double.class).name("zOffset").nodePath("offsetZ").fallback(0d)
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var origin = context.get(LootContextKeys.ORIGIN);

        return origin == null ||
                predicate.test(context.assure(LootContextKeys.WORLD), origin.add(xOffset, yOffset, zOffset));
    }
}

