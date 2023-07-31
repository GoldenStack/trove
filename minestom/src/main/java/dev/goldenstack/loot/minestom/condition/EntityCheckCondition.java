package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.RelevantEntity;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.type;

/**
 * Checks the {@link #chosenEntity()} with the {@link #entityChecker()}.
 * @param chosenEntity the specific entity to select
 * @param entityChecker the predicate that checks the entity
 */
public record EntityCheckCondition(@NotNull RelevantEntity chosenEntity,
                                   @NotNull VanillaInterface.EntityPredicate entityChecker) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:entity_properties";

    /**
     * A standard map-based converter entity check conditions.
     */
    public static final @NotNull TypedLootConverter<EntityCheckCondition> CONVERTER =
            converter(EntityCheckCondition.class,
                    type(RelevantEntity.class).name("chosenEntity").nodePath("entity"),
                    type(VanillaInterface.EntityPredicate.class).name("entityChecker").nodePath("predicate")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var entity = context.get(chosenEntity.key());
        var origin = context.get(LootContextKeys.ORIGIN);

        var world = context.assure(LootContextKeys.WORLD);

        return entityChecker.test(world, origin, entity);
    }
}
