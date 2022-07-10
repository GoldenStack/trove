package dev.goldenstack.loot.minestom.requirement;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.conversion.LootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootRequirement;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Returns true if any provide context instances have {@link LootContextKeys#LAST_DAMAGE_PLAYER}.
 */
public record KilledByPlayerRequirement() implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, KilledByPlayerRequirement> CONVERTER = new LootConverter<>("minecraft:killed_by_player", KilledByPlayerRequirement.class) {

        @Override
        public @NotNull KilledByPlayerRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            return new KilledByPlayerRequirement();
        }

        @Override
        public void serialize(@NotNull KilledByPlayerRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {

        }
    };

    /**
     * @param context the loot context that will be verified
     * @return true if the provided context has {@link LootContextKeys#LAST_DAMAGE_PLAYER}
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        return context.has(LootContextKeys.LAST_DAMAGE_PLAYER);
    }
}
