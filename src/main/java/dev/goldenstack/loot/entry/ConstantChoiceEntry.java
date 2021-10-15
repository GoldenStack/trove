package dev.goldenstack.loot.entry;

import com.google.common.collect.ImmutableList;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.function.LootFunction;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A type of LootEntry that would always return a single, constant Choice. This class automatically creates a Choice
 * instance for you and calls the {@link #generateLoot(LootContext)} method every time the choice is triggered.
 */
public abstract class ConstantChoiceEntry extends LootEntry {

    /**
     * A choice that always calls {@link #generateLoot(LootContext)} to generate its results.
     */
    private class InnerChoice extends Choice {
        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            return ConstantChoiceEntry.this.generateLoot(context);
        }

        @Contract(pure = true)
        @Override
        public @NotNull String toString() {
            return "ConstantChoiceEntry.InnerChoice[entry=" + ConstantChoiceEntry.this + "]";
        }
    }

    private final @NotNull ImmutableList<Choice> choices = ImmutableList.of(new InnerChoice());

    /**
     * Create a ConstantChoiceEntry with the provided conditions, functions, weight, and quality.
     */
    public ConstantChoiceEntry(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<LootFunction> functions, int weight, int quality){
        super(conditions, functions, weight, quality);
    }

    /**
     * Returns this ConstantChoiceEntry's choice list. This choice list is an internal constant and the same value is
     * always returned.
     */
    @Override
    protected final @NotNull ImmutableList<Choice> collectChoices(@NotNull LootContext context) {
        return choices;
    }

    /**
     * Generates loot based on the provided LootContext. This is called internally by the choice that is returned from
     * {@link #collectChoices(LootContext)}
     */
    public abstract @NotNull List<ItemStack> generateLoot(@NotNull LootContext context);
}