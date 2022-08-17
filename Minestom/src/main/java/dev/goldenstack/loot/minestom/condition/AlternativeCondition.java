package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Returns true if at least one of the {@link #conditions()} returns true.
 * @param conditions the conditions that are tested
 */
public record AlternativeCondition(@NotNull List<LootCondition<ItemStack>> conditions) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for alternative conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, AlternativeCondition> CONVERTER = Utils.createKeyedConverter("minecraft:alternative", new TypeToken<>(){},
            (input, result, context) ->
                    result.node("terms").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context)),
            (input, context) -> new AlternativeCondition(
                    Utils.deserializeList(input.node("terms"), context.loader().lootConditionManager()::deserialize, context)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return LootCondition.or(conditions(), context);
    }
}
