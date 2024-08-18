package net.goldenstack.loot;


import net.goldenstack.loot.util.ItemPredicate;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/**
 * A function that allows loot to pass through it, potentially making modifications.
 */
public interface LootFunction {

    /**
     * Performs any mutations on the provided object and returns the result.
     * @param input the input item to this function
     * @param context the context object, to use if required
     * @return the modified form of the input
     */
    @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context);

    record All(@NotNull List<LootFunction> functions) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            for (var function : functions) {
                input = function.apply(input, context);
            }
            return input;
        }
    }

    record Filtered(@NotNull List<LootPredicate> predicates, @NotNull ItemPredicate predicate, @NotNull LootFunction function) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            return LootPredicate.all(predicates, context) && predicate.test(input) ?
                    function.apply(input, context) : input;
        }
    }

    record SetPotion(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID key) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            PotionContents existing = input.get(ItemComponent.POTION_CONTENTS, PotionContents.EMPTY);
            PotionContents updated = new PotionContents(PotionType.fromNamespaceId(key), existing.customColor(), existing.customEffects());

            return input.with(ItemComponent.POTION_CONTENTS, updated);
        }
    }

    record ExplosionDecay(@NotNull List<LootPredicate> predicates) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            if (radius == null) return input;

            Random random = context.require(LootContext.RANDOM);

            float chance = 1 / radius;
            int trials = input.amount();

            int newAmount = 0;

            for (int i = 0; i < trials; i++) {
                if (random.nextFloat() <= chance) {
                    newAmount++;
                }
            }

            return input.withAmount(newAmount);
        }
    }



}
