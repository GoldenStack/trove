package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that always returns a single, constant choice. In this case, it returns a list containing just itself.
 * @param <L> the loot item type
 */
public interface SingleChoiceEntry<L> extends LootEntry<L>, LootEntry.Choice<L> {

    @Override
    default @NotNull List<Choice<L>> requestChoices(@NotNull LootGenerationContext context) {
        return List.of(this);
    }

}
