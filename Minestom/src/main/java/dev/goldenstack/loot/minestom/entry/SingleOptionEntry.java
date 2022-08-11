package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An entry that always returns a single, constant option. In this case, it returns a list containing just itself.
 * @param <L> the loot item type
 */
public interface SingleOptionEntry<L> extends LootEntry<L>, LootEntry.Option<L> {

    @Override
    default @NotNull List<Option<L>> requestOptions(@NotNull LootGenerationContext context) {
        return List.of(this);
    }

}
