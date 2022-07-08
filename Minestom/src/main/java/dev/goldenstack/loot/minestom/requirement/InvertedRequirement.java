package dev.goldenstack.loot.minestom.requirement;

import com.google.gson.JsonObject;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.conversion.LootConverter;
import dev.goldenstack.loot.structure.LootRequirement;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Inverts the result of {@link #requirement()}.
 * @param requirement the requirement to invert
 */
public record InvertedRequirement(@NotNull LootRequirement<ItemStack> requirement) implements LootRequirement<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, InvertedRequirement> CONVERTER = new LootConverter<>("minecraft:inverted", InvertedRequirement.class) {
        @Override
        public @NotNull InvertedRequirement deserialize(@NotNull JsonObject json, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            return new InvertedRequirement(context.loader().lootRequirementManager().deserialize(json.get("term"), context));
        }

        @Override
        public void serialize(@NotNull InvertedRequirement input, @NotNull JsonObject result, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            result.add("term", context.loader().lootRequirementManager().serialize(input.requirement(), context));
        }
    };

    /**
     * @param context the loot context that will be verified
     * @return the inverted result of {@code requirement().check(context)}
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        return !requirement.check(context);
    }
}
