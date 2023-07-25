package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    public static final @NotNull LootConverter<LootPool> CONVERTER =
            converter(LootPool.class,
                    number().name("rolls"),
                    number().name("bonusRolls").nodePath("bonus_rolls").withDefault(new ConstantNumber(0)),
                    entry().list().name("entries"),
                    condition().list().name("conditions").withDefault(List::of),
                    modifier().list().name("modifiers").nodePath("functions").withDefault(List::of)
            ).converter();

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

        List<Object> items = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            var generated = LootEntry.pickChoice(entries, context);
            if (generated != null) {
                items.addAll(generated.generate(context).items());
            }
        }

        return LootModifier.applyAll(modifiers(), new LootBatch(items), context);
    }

}