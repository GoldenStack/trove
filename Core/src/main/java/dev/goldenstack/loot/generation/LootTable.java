package dev.goldenstack.loot.generation;

/**
 * A loot table that can generate loot on demand.<br>
 * This is a separate interface even though it declares no new methods so that it can be easily distinguished and so
 * that anything that would normally be a {@link LootGenerator} could not accidentally be used as this.
 * @param <L> the loot item type
 */
public interface LootTable<L> extends LootGenerator<L> {

}
