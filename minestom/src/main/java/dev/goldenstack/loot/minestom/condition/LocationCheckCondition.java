package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.implicit;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.locationPredicate;

/**
 * Verifies that the location at the context (i.e. the {@link LootContextKeys#WORLD} and {@link LootContextKeys#ORIGIN})
 * is valid according to the {@link #predicate()}.
 */
public record LocationCheckCondition(@NotNull VanillaInterface.LocationPredicate predicate,
                                     double xOffset, double yOffset, double zOffset) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:location_check";

    /**
     * A standard map-based converter for location check conditions.
     */
    public static final @NotNull TypedLootConverter<LocationCheckCondition> CONVERTER =
            converter(LocationCheckCondition.class,
                    locationPredicate().name("predicate"),
                    implicit(double.class).name("xOffset").nodePath("offsetX").withDefault(0d),
                    implicit(double.class).name("yOffset").nodePath("offsetY").withDefault(0d),
                    implicit(double.class).name("zOffset").nodePath("offsetZ").withDefault(0d)
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var origin = context.get(LootContextKeys.ORIGIN);

        return origin == null ||
                predicate.test(context.assure(LootContextKeys.WORLD), origin.add(xOffset, yOffset, zOffset));
    }
}

