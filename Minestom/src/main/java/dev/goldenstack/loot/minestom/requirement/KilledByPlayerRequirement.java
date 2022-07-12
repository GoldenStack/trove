package dev.goldenstack.loot.minestom.requirement;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootRequirement;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Returns true if any provide context instances have {@link LootContextKeys#LAST_DAMAGE_PLAYER}.
 */
public record KilledByPlayerRequirement() implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, KilledByPlayerRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:killed_by_player", TypeToken.get(KilledByPlayerRequirement.class)) {

        @Override
        public @NotNull KilledByPlayerRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new KilledByPlayerRequirement();
        }

        @Override
        public void serialize(@NotNull KilledByPlayerRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {

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
