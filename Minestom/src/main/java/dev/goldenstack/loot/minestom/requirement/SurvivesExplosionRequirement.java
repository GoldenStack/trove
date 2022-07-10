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
 * Randomly decides whether or not something survives an explosion. The formula is simple; the probability just
 * decreases the larger the explosion gets, approaching zero.
 */
public record SurvivesExplosionRequirement() implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, SurvivesExplosionRequirement> CONVERTER = new LootConverter<>("minecraft:survives_explosion", SurvivesExplosionRequirement.class) {

        @Override
        public @NotNull SurvivesExplosionRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            return new SurvivesExplosionRequirement();
        }

        @Override
        public void serialize(@NotNull SurvivesExplosionRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {

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
