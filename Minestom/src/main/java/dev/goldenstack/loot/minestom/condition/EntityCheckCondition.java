package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.MinestomTypes;
import dev.goldenstack.loot.minestom.util.RelevantEntity;
import dev.goldenstack.loot.structure.LootCondition;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;

/**
 * Checks the {@link #relevantEntity()} with the {@link #entityPredicate()}.
 * @param relevantEntity the specific entity to select
 * @param entityPredicate the predicate that checks the entity
 */
public record EntityCheckCondition(@NotNull RelevantEntity relevantEntity,
                                   @NotNull VanillaInterface.EntityPredicate entityPredicate) implements LootCondition {

    /**
     * A standard map-based converter entity check conditions.
     */
    public static final @NotNull KeyedLootConverter<EntityCheckCondition> CONVERTER =
            converter(EntityCheckCondition.class,
                    MinestomTypes.relevantEntity().name("relevantEntity").nodePath("entity"),
                    MinestomTypes.entityPredicate().name("entityPredicate").nodePath("predicate")
            ).keyed("minecraft:entity_properties");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var entity = context.get(relevantEntity.key());
        var origin = context.get(LootContextKeys.ORIGIN);

        var world = context.assure(LootContextKeys.WORLD);

        return entityPredicate.test(world, origin, entity);
    }
}
