package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.RelevantEntity;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.entityPredicate;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.relevantEntity;

/**
 * Checks the {@link #chosenEntity()} with the {@link #entityChecker()}.
 * @param chosenEntity the specific entity to select
 * @param entityChecker the predicate that checks the entity
 */
public record EntityCheckCondition(@NotNull RelevantEntity chosenEntity,
                                   @NotNull VanillaInterface.EntityPredicate entityChecker) implements LootCondition {

    /**
     * A standard map-based converter entity check conditions.
     */
    public static final @NotNull KeyedLootConverter<EntityCheckCondition> CONVERTER =
            converter(EntityCheckCondition.class,
                    relevantEntity().name("chosenEntity").nodePath("entity"),
                    entityPredicate().name("entityChecker").nodePath("predicate")
            ).keyed("minecraft:entity_properties");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var entity = context.get(chosenEntity.key());
        var origin = context.get(LootContextKeys.ORIGIN);

        var world = context.assure(LootContextKeys.WORLD);

        return entityChecker.test(world, origin, entity);
    }
}
