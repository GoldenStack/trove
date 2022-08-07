package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.generation.LootPool;
import dev.goldenstack.loot.generation.LootTable;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.minestom.context.LootConversionKeys;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.util.Utils;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard loot table implementation for Minestom.
 * @param contextKeyGroup the context key group that all provided context objects must pass
 * @param pools the pools that will generate loot
 * @param modifiers the modifiers that are applied to each piece of loot
 */
public record StandardLootTable(@NotNull LootContextKeyGroup contextKeyGroup,
                                @NotNull List<LootPool<ItemStack>> pools,
                                @NotNull List<LootModifier<ItemStack>> modifiers) implements LootTable<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, LootTable<ItemStack>> CONVERTER = new LootConverter<>() {
        @Override
        public @NotNull ConfigurationNode serialize(@NotNull LootTable<ItemStack> input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            if (!(input instanceof StandardLootTable table)) {
                throw new ConfigurateException("Expected type " + StandardLootTable.class + " but found " + input.getClass() + " for the provided loot table");
            }
            var node = context.loader().createNode();
            node.node("type").set(table.contextKeyGroup().id());
            node.node("pools").set(Utils.serializeList(table.pools(), context.loader().lootPoolConverter(), context));
            node.node("functions").set(Utils.serializeList(table.modifiers(), context.loader().lootModifierManager()::serialize, context));
            return node;
        }

        @Override
        public @NotNull LootTable<ItemStack> deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new StandardLootTable(
                    context.assure(LootConversionKeys.CONTEXT_KEYS).get(Utils.require(input.node("type"), String.class)),
                    Utils.deserializeList(input.node("pools"), context.loader().lootPoolConverter(),  context),
                    Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context)
            );
        }
    };

    public StandardLootTable {
        pools = List.copyOf(pools);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        // Make sure that this table's required keys are in the given context
        contextKeyGroup.assureVerified(context);

        if (pools.isEmpty()) {
            return List.of();
        }

        List<ItemStack> items = new ArrayList<>();
        for (var pool : pools) {
            for (var item : pool.generate(context)) {
                items.add(LootModifier.applyAll(modifiers(), item, context));
            }
        }

        return items;
    }
}
