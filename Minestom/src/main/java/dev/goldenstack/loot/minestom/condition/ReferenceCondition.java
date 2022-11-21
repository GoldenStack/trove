package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * An entry that is dynamically linked to a loot condition using {@link LootContextKeys#REGISTERED_CONDITIONS} and {@link #id()}.
 * @param id the ID to get from the map
 */
public record ReferenceCondition(@NotNull NamespaceID id) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for reference conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, ReferenceCondition> CONVERTER = Utils.createKeyedConverter("minecraft:reference", new TypeToken<>(){},
            (input, result, context) ->
                    result.node("name").set(input.id.asString()),
            (input, context) -> new ReferenceCondition(
                    NamespaceID.from(input.node("name").require(String.class))
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        var registered = context.assure(LootContextKeys.REGISTERED_CONDITIONS);
        var condition = registered.get(this.id);
        return condition != null && condition.verify(context);
    }
}
