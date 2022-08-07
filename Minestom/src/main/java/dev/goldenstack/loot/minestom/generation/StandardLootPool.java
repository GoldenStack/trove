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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A standard loot pool implementation for Minestom.
 * @param rolls the normal number of rolls that should occur
 * @param bonusRolls the number of extra rolls that will occur. This is multiplied by the context's luck.
 * @param entries the entries to generate loot from
 * @param conditions the conditions that determine if any loot should be generated
 * @param modifiers the modifiers that are applied to each piece of loot
 */
public record StandardLootPool(@NotNull LootNumber<ItemStack> rolls,
                               @NotNull LootNumber<ItemStack> bonusRolls,
                               @NotNull List<LootEntry<ItemStack>> entries,
                               @NotNull List<LootCondition<ItemStack>> conditions,
                               @NotNull List<LootModifier<ItemStack>> modifiers) implements LootPool<ItemStack> {

    public static final @NotNull LootConverter<ItemStack, LootPool<ItemStack>> CONVERTER = new LootConverter<>() {
        @Override
        public @NotNull ConfigurationNode serialize(@NotNull LootPool<ItemStack> input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            if (!(input instanceof StandardLootPool pool)) {
                throw new ConfigurateException("Expected type " + StandardLootPool.class + " but found " + input.getClass() + " for the provided loot pool");
            }
            var node = context.loader().createNode();
            node.node("rolls").set(context.loader().lootNumberManager().serialize(pool.rolls(), context));
            node.node("bonus_rolls").set(context.loader().lootNumberManager().serialize(pool.bonusRolls(), context));
            node.node("functions").set(Utils.serializeList(pool.modifiers(), context.loader().lootModifierManager()::serialize, context));
            node.node("entries").set(Utils.serializeList(pool.entries(), context.loader().lootEntryManager()::serialize, context));
            node.node("conditions").set(Utils.serializeList(pool.conditions(), context.loader().lootConditionManager()::serialize, context));
            return node;
        }

        @Override
        public @NotNull LootPool<ItemStack> deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return new StandardLootPool(
                    context.loader().lootNumberManager().deserialize(input.node("rolls"), context),
                    input.hasChild("bonus_rolls") ? context.loader().lootNumberManager().deserialize(input.node("bonus_rolls"), context) : new ConstantNumber(0),
                    Utils.deserializeList(input.node("entries"), context.loader().lootEntryManager()::deserialize, context),
                    Utils.deserializeList(input.node("conditions"), context.loader().lootConditionManager()::deserialize, context),
                    Utils.deserializeList(input.node("functions"), context.loader().lootModifierManager()::deserialize, context)
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

    /**
     * Creates a new builder for this class, with no entries, conditions, or modifiers, and null rolls and bonus rolls.
     * When building, it's acceptable to leave {@link Builder#bonusRolls} as null because it will be replaced with zero.
     * <br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new StandardLootPool builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LootNumber<ItemStack> rolls, bonusRolls;
        private final @NotNull List<LootEntry<ItemStack>> entries = new ArrayList<>();
        private final @NotNull List<LootCondition<ItemStack>> conditions = new ArrayList<>();
        private final @NotNull List<LootModifier<ItemStack>> modifiers = new ArrayList<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder rolls(@NotNull LootNumber<ItemStack> rolls) {
            this.rolls = rolls;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder bonusRolls(@NotNull LootNumber<ItemStack> bonusRolls) {
            this.bonusRolls = bonusRolls;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addEntry(@NotNull LootEntry<ItemStack> entry) {
            this.entries.add(entry);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addCondition(@NotNull LootCondition<ItemStack> condition) {
            this.conditions.add(condition);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addModifier(@NotNull LootModifier<ItemStack> modifier) {
            this.modifiers.add(modifier);
            return this;
        }

        @Contract(" -> new")
        public @NotNull StandardLootPool build() {
            return new StandardLootPool(
                    Objects.requireNonNull(rolls, "Standard loot pools must have a number of rolls!"),
                    Objects.requireNonNullElseGet(bonusRolls, () -> new ConstantNumber(0)),
                    entries,
                    conditions,
                    modifiers
            );
        }

    }
}
