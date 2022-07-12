package dev.goldenstack.loot.minestom.requirement;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootRequirement;
import dev.goldenstack.loot.util.NodeUtils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

/**
 * Assures that at least one of the requirements in {@link #terms()} is true.
 * @param terms the list of requirements to check
 */
public record AlternativeRequirement(@NotNull List<LootRequirement<ItemStack>> terms) implements LootRequirement<ItemStack> {

    public static final @NotNull KeyedLootConverter<ItemStack, AlternativeRequirement> CONVERTER = new KeyedLootConverter<>("minecraft:alternative", TypeToken.get(AlternativeRequirement.class)) {
        @Override
        public @NotNull AlternativeRequirement deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new AlternativeRequirement(
                    NodeUtils.deserializeList(node.node("terms"), context.loader().lootRequirementManager()::deserialize, context)
            );
        }

        @Override
        public void serialize(@NotNull AlternativeRequirement input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("terms").set(NodeUtils.serializeList(input.terms(), context.loader().lootRequirementManager()::serialize, context));
        }
    };

    public AlternativeRequirement {
        terms = List.copyOf(terms);
    }

    /**
     * @param context the loot context that will be verified
     * @return true if at least one of the requirements in {@link #terms()} returns true
     */
    @Override
    public boolean check(@NotNull LootContext context) {
        return LootRequirement.or(context, terms);
    }
}
