package dev.goldenstack.loot.minestom.generation;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.generation.LootGenerator;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.number.ConstantNumber;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootEntry;
import dev.goldenstack.loot.structure.LootModifier;
import dev.goldenstack.loot.structure.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;
import java.util.function.Consumer;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

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

    public static final @NotNull TypeSerializer<LootPool> SERIALIZER =
            serializer(LootPool.class,
                    field(LootNumber.class).name("rolls"),
                    field(LootNumber.class).name("bonusRolls").nodePath("bonus_rolls").fallback(new ConstantNumber(0)),
                    field(LootEntry.class).name("entries").as(list()),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(LootModifier.class).name("modifiers").nodePath("functions").as(list()).fallback(List::of)
            );

    public LootPool {
        entries = List.copyOf(entries);
        conditions = List.copyOf(conditions);
        modifiers = List.copyOf(modifiers);
    }

    @Override
    public void accept(@NotNull LootContext context, @NotNull Consumer<@NotNull Object> processor) {
        if (!LootCondition.all(conditions, context)) return;

        long rolls = this.rolls.getLong(context);

        Double luck = context.get(LootContextKeys.LUCK);
        if (luck != null) {
            rolls += (long) Math.floor(luck * this.bonusRolls.getDouble(context));
        }

        for (int i = 0; i < rolls; i++) {
            var generated = LootEntry.pickChoice(entries, context);
            if (generated != null) {
                generated.accept(context, object -> processor.accept(LootModifier.apply(modifiers(), object, context)));
            }
        }
    }
}
