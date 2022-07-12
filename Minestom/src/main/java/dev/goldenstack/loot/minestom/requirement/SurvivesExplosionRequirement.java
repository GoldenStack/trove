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
 * Randomly decides whether or not something survives an explosion. The formula is simple; the probability just
 * decreases the larger the explosion gets, approaching zero.
 */
public record SurvivesExplosionRequirement() implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, SurvivesExplosionRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:survives_explosion", TypeToken.get(SurvivesExplosionRequirement.class)) {

        @Override
        public @NotNull SurvivesExplosionRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new SurvivesExplosionRequirement();
        }

        @Override
        public void serialize(@NotNull SurvivesExplosionRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {

        }
    };

    /**
     * @param context the loot context that will be verified
     * @return true if a randomly generated number is less than {@code 1 / [the explosion radius]}
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        Float radius = context.get(LootContextKeys.EXPLOSION_RADIUS);
        return radius == null || context.random().nextFloat() <= 1 / radius;
    }
}
