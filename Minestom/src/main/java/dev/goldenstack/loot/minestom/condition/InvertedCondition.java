package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Inverts the result of the child condition
 * @param condition the condition to invert
 */
public record InvertedCondition(@NotNull LootCondition<ItemStack> condition) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for inverted conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, InvertedCondition> CONVERTER = Utils.createKeyedConverter("minecraft:inverted", new TypeToken<>(){},
            (input, result, context) ->
                    result.node("term").set(context.loader().lootConditionManager().serialize(input.condition, context)),
            (input, context) -> new InvertedCondition(
                    context.loader().lootConditionManager().deserialize(input.node("term"), context)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return !condition.verify(context);
    }
}
