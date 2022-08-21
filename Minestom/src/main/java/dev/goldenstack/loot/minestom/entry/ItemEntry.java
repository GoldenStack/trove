package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;

import java.util.List;

/**
 * An entry that always returns an item of its material.
 * @param material the material that is used for items
 * @param weight the base weight of this entry - see {@link StandardWeightedOption#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedOption#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record ItemEntry(@NotNull Material material,
                        long weight, long quality,
                        @NotNull List<LootModifier<ItemStack>> modifiers,
                        @NotNull List<LootCondition<ItemStack>> conditions) implements SingleOptionEntry<ItemStack>, StandardWeightedOption<ItemStack> {

    /**
     * A standard map-based converter for item entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, ItemEntry> CONVERTER = Utils.createKeyedConverter("minecraft:item", new TypeToken<>(){},
            (input, result, context) -> {
                result.node("name").set(input.material.namespace().asString());
                result.node("weight").set(input.weight);
                result.node("quality").set(input.quality);
                result.node("functions").set(Utils.serializeList(input.modifiers(), context.loader().lootModifierManager()::serialize, context));
                result.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
            }, (input, context) -> {
                var nameNode = input.node("name");
                String name = nameNode.require(String.class);
                Material material = Material.fromNamespaceId(name);
                if (material == null) {
                    throw new ConfigurateException(nameNode, "Expected the provided node to have a valid material, but found '" + name + "' instead.");
                }
                return new ItemEntry(
                        material,
                        input.node("weight").require(Long.class),
                        input.node("quality").require(Long.class),
                        Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context),
                        Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context)
                );
            });

    public ItemEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        return LootCondition.all(conditions(), context) ?
                List.of(LootModifier.applyAll(modifiers(), ItemStack.of(material), context)) :
                List.of();
    }
}