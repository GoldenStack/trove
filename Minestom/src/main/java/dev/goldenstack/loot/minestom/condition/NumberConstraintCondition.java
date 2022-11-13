package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.LootNumberRange;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Verifies that the result of the provided value is within the provided range.
 * @param range the range of valid values
 * @param value the value to verify
 */
public record NumberConstraintCondition(@NotNull LootNumberRange range, @NotNull LootNumber<ItemStack> value) implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for value check conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, NumberConstraintCondition> CONVERTER = Utils.createKeyedConverter("minecraft:value_check", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("range").set(LootNumberRange.CONVERTER.serialize(input.range(), context));
                result.node("value").set(context.loader().lootNumberManager().serialize(input.value(), context));
            },
            (input, context) -> new NumberConstraintCondition(
                    LootNumberRange.CONVERTER.deserialize(input.node("range"), context),
                    context.loader().lootNumberManager().deserialize(input.node("value"), context)
            ));

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return range.check(context, value.getLong(context));
    }
}

