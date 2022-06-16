package dev.goldenstack.loot.json;

/**
 * Simple interface used to represent something that is aware of the fact that it should have a specified loot item.
 * This is generally used for serialization and deserialization.
 * @param <L> the loot item
 */
public interface LootAware<L> {}
