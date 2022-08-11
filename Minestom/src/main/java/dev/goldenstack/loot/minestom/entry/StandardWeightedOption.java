package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * An option that uses the standard method of generating weight - adding the {@link #weight()} to the {@link #quality()}
 * where the quality is multiplied by the provided context's luck ({@link LootContextKeys#LUCK}).
 * @param <L> the loot item type
 */
public interface StandardWeightedOption<L> extends LootEntry.Option<L> {

    /**
     * The weight of this option. When calculating the final weight, this value is simply added to the result.
     * @return the base weight of this option
     */
    @Range(from = 1L, to = Long.MAX_VALUE) long weight();

    /**
     * The quality of the option. When calculating the final weight, this number is multiplied by the context's luck
     * value, which is stored at the key {@link LootContextKeys#LUCK}.
     * @return the quality of the option
     */
    @Range(from = 0L, to = Long.MAX_VALUE) long quality();

    @Override
    default @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootGenerationContext context) {
        return Math.max(1, (long) Math.floor(weight() + quality() * context.assure(LootContextKeys.LUCK)));
    }

}
