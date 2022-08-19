package dev.goldenstack.loot.minestom.condition;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that simply requires a player to have been involved in the killing of the entity somehow.
 */
public record KilledByPlayerCondition() implements LootCondition<ItemStack> {

    /**
     * A standard map-based converter for killed-by-player conditions.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, KilledByPlayerCondition> CONVERTER = Utils.createKeyedConverter("minecraft:killed_by_player", new TypeToken<>(){},
            (input, result, context) -> {},
            (input, context) -> new KilledByPlayerCondition());

    @Override
    public boolean verify(@NotNull LootGenerationContext context) {
        return context.has(LootContextKeys.LAST_DAMAGE_PLAYER);
    }
}
