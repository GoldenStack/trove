package dev.goldenstack.loot.minestom.requirement;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootRequirement;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Inverts the result of {@link #requirement()}.
 * @param requirement the requirement to invert
 */
public record InvertedRequirement(@NotNull LootRequirement<ItemStack> requirement) implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, InvertedRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:inverted", TypeToken.get(InvertedRequirement.class)) {
        @Override
        public @NotNull InvertedRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new InvertedRequirement(
                    context.loader().lootRequirementManager().deserialize(node.node("term"), context)
            );
        }

        @Override
        public void serialize(@NotNull InvertedRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("term").set(context.loader().lootRequirementManager().serialize(input.requirement(), context));
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
