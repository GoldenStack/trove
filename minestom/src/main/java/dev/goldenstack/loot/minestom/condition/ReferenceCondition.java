package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;

/**
 * An entry that is dynamically linked to a loot condition using {@link LootContextKeys#REGISTERED_CONDITIONS} and {@link #id()}.
 * @param id the ID to get from the map
 */
public record ReferenceCondition(@NotNull NamespaceID id) implements LootCondition {

    public static final @NotNull String KEY = "minecraft:reference";

    /**
     * A standard map-based converter for reference conditions.
     */
    public static final @NotNull TypedLootConverter<ReferenceCondition> CONVERTER =
            converter(ReferenceCondition.class,
                    field(NamespaceID.class).name("id").nodePath("name")
            );

    @Override
    public boolean verify(@NotNull LootContext context) {
        var registered = context.assure(LootContextKeys.REGISTERED_CONDITIONS);
        var condition = registered.get(this.id);
        return condition != null && condition.verify(context);
    }
}
