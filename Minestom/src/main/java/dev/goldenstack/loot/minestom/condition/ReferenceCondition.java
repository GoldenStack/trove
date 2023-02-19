package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.namespaceId;

/**
 * An entry that is dynamically linked to a loot condition using {@link LootContextKeys#REGISTERED_CONDITIONS} and {@link #id()}.
 * @param id the ID to get from the map
 */
public record ReferenceCondition(@NotNull NamespaceID id) implements LootCondition {

    /**
     * A standard map-based converter for reference conditions.
     */
    public static final @NotNull KeyedLootConverter<ReferenceCondition> CONVERTER =
            converter(ReferenceCondition.class,
                    namespaceId().name("id").nodeName("name")
            ).keyed("minecraft:reference");

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var registered = context.assure(LootContextKeys.REGISTERED_CONDITIONS);
        var condition = registered.get(this.id);
        return condition != null && condition.verify(context);
    }
}
