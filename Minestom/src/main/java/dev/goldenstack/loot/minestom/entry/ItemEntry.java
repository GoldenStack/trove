package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

/**
 * An entry that always returns a singular item with the material {@link #itemMaterial()}
 * @param itemMaterial the material of the singular generated item
 * @param weight the base weight of this entry - see {@link StandardWeightedOption#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedOption#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 */
public record ItemEntry(@NotNull Material itemMaterial, long weight, long quality, @NotNull List<LootModifier<ItemStack>> modifiers) implements SingleOptionEntry<ItemStack>, StandardWeightedOption<ItemStack> {

    /**
     * A standard map-based converter for item entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, ItemEntry> CONVERTER = new KeyedLootConverter<>("minecraft:item", TypeToken.get(ItemEntry.class)) {
        @Override
        public void serialize(@NotNull ItemEntry input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("name").set(input.itemMaterial.namespace().asString());
            result.node("weight").set(input.weight);
            result.node("quality").set(input.quality);
            result.node("functions").set(Utils.serializeList(input.modifiers(), context.loader().lootModifierManager()::serialize, context));
        }

        @Override
        public @NotNull ItemEntry deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            var nameNode = input.node("name");
            String name = Utils.require(nameNode, String.class);
            Material material = Material.fromNamespaceId(name);
            if (material == null) {
                throw new ConfigurateException(nameNode, "Expected the provided node to have a valid material, but found '" + name + "' instead.");
            }
            return new ItemEntry(
                    material,
                    input.node("weight").getLong(1),
                    input.node("quality").getLong(0),
                    Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context)
            );
        }
    };

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        return List.of(ItemStack.of(itemMaterial));
    }
}