package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.util.Utils;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

/**
 * An entry that always returns an empty list of items.
 * @param weight the base weight of this entry - see {@link StandardWeightedOption#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedOption#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 */
public record EmptyEntry(long weight, long quality, @NotNull List<LootModifier<ItemStack>> modifiers) implements SingleOptionEntry<ItemStack>, StandardWeightedOption<ItemStack> {

    /**
     * A standard map-based converter for empty entries.
     */
    public static final @NotNull KeyedLootConverter<ItemStack, EmptyEntry> CONVERTER = new KeyedLootConverter<>("minecraft:empty", TypeToken.get(EmptyEntry.class)) {
        @Override
        public void serialize(@NotNull EmptyEntry input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            result.node("weight").set(input.weight);
            result.node("quality").set(input.quality);
            result.node("functions").set(Utils.serializeList(input.modifiers(), context.loader().lootModifierManager()::serialize, context));
        }

        @Override
        public @NotNull EmptyEntry deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new EmptyEntry(
                    input.node("weight").getLong(1),
                    input.node("quality").getLong(0),
                    Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context)
            );
        }
    };

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        return List.of();
    }
}
