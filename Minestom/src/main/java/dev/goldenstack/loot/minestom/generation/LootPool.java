package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import dev.goldenstack.loot.util.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * A standard loot pool implementation for Minestom.
 * @param rolls the normal number of rolls that should occur
 * @param bonusRolls the number of extra rolls that will occur. This is multiplied by the context's luck.
 * @param entries the entries to generate loot from
 * @param conditions the conditions that determine if any loot should be generated
 * @param modifiers the modifiers that are applied to each piece of loot
 */
public record LootPool(@NotNull LootNumber rolls,
                       @NotNull LootNumber bonusRolls,
                       @NotNull List<LootEntry> entries,
                       @NotNull List<LootCondition> conditions,
                       @NotNull List<LootModifier> modifiers) implements LootGenerator {

    public static final @NotNull AdditiveConverter<LootPool> CONVERTER =
            converter(LootPool.class,
                    number().name("rolls"),
                    number().name("bonusRolls").nodeName("bonus_rolls").withDefault(new ConstantNumber(0)),
                    entry().list().name("entries"),
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    modifier().list().name("modifiers").nodeName("functions").withDefault(ArrayList::new)
            ).additive();

    public LootPool {
        entries = List.copyOf(entries);
        conditions = List.copyOf(conditions);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions, context)) {
            return LootBatch.of();
        }

        long rolls = this.rolls.getLong(context);

        Double luck = context.get(LootContextKeys.LUCK);
        if (luck != null) {
            rolls += Math.floor(luck * this.bonusRolls.getDouble(context));
        }

        LootBatch loot = Utils.generateStandardLoot(this.entries, rolls, context);
        return LootModifier.applyAll(modifiers(), loot, context);
    }

    /**
     * Creates a new builder for this class, with no entries, conditions, or modifiers, and null rolls and bonus rolls.
     * When building, it's acceptable to leave {@link Builder#bonusRolls} as null because it will be replaced with zero.
     * <br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new loot pool builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LootNumber rolls, bonusRolls;
        private final @NotNull List<LootEntry> entries = new ArrayList<>();
        private final @NotNull List<LootCondition> conditions = new ArrayList<>();
        private final @NotNull List<LootModifier> modifiers = new ArrayList<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder rolls(@NotNull LootNumber rolls) {
            this.rolls = rolls;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder bonusRolls(@NotNull LootNumber bonusRolls) {
            this.bonusRolls = bonusRolls;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addEntry(@NotNull LootEntry entry) {
            this.entries.add(entry);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addCondition(@NotNull LootCondition condition) {
            this.conditions.add(condition);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addModifier(@NotNull LootModifier modifier) {
            this.modifiers.add(modifier);
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootPool build() {
            return new LootPool(
                    Objects.requireNonNull(rolls, "Standard loot pools must have a number of rolls!"),
                    Objects.requireNonNullElseGet(bonusRolls, () -> new ConstantNumber(0)),
                    entries,
                    conditions,
                    modifiers
            );
        }

    }
}