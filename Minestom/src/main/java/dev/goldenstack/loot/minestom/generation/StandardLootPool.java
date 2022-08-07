package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.generation.LootPool;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

/**
 * A standard loot pool implementation.
 * @param entries the entries to generate loot from
 * @param conditions the conditions that determine if any loot should be generated
 * @param modifiers the modifiers that are applied to each piece of loot
 * @param rolls the normal number of rolls that should occur
 * @param bonusRolls the number of extra rolls that will occur. This is multiplied by the context's luck.
 */
public record StandardLootPool(@NotNull List<LootEntry<ItemStack>> entries,
                               @NotNull List<LootCondition<ItemStack>> conditions,
                               @NotNull List<LootModifier<ItemStack>> modifiers,
                               @NotNull LootNumber<ItemStack> rolls,
                               @NotNull LootNumber<ItemStack> bonusRolls) implements LootPool<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, StandardLootPool> CONVERTER = new LootConverter<>() {
        @Override
        public @NotNull ConfigurationNode serialize(@NotNull StandardLootPool input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            var node = context.loader().createNode();
            node.node("entries").set(Utils.serializeList(input.entries(), context.loader().lootEntryManager()::serialize, context));
            node.node("conditions").set(Utils.serializeList(input.conditions(), context.loader().lootConditionManager()::serialize, context));
            node.node("functions").set(Utils.serializeList(input.modifiers(), context.loader().lootModifierManager()::serialize, context));
            node.node("rolls").set(context.loader().lootNumberManager().serialize(input.rolls(), context));
            node.node("bonus_rolls").set(context.loader().lootNumberManager().serialize(input.bonusRolls(), context));
            return node;
        }

        @Override
        public @NotNull StandardLootPool deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new StandardLootPool(
                    Utils.deserializeList(input.node("entries"), context.loader().lootEntryManager()::deserialize, context),
                    Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context),
                    Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context),
                    context.loader().lootNumberManager().deserialize(input.node("rolls"), context),
                    input.hasChild("bonus_rolls") ? context.loader().lootNumberManager().deserialize(input.node("bonus_rolls"), context) : new ConstantNumber(0)
            );
        }
    };

    public StandardLootPool {
        entries = List.copyOf(entries);
        conditions = List.copyOf(conditions);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions, context)) {
            return List.of();
        }

        long rolls = this.rolls.getLong(context);

        Double luck = context.get(LootContextKeys.LUCK);
        if (luck != null) {
            rolls += Math.floor(luck * this.bonusRolls.getDouble(context));
        }

        List<ItemStack> loot = Utils.generateStandardLoot(this.entries, rolls, context);

        if (!loot.isEmpty()) {
            loot.replaceAll(lootItem -> LootModifier.applyAll(this.modifiers, lootItem, context));
        }
        return loot;
    }
}
