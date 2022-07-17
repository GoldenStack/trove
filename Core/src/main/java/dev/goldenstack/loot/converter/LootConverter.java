package dev.goldenstack.loot.converter;

/**
 * A standard representation of something that can convert a type of object ({@link V}) to and from a configuration
 * node. This is a simple combination of {@link LootSerializer} and {@link LootDeserializer}.
 * @param <L> the loot item type
 * @param <V> the type of object that will be serialized and deserialized
 */
public interface LootConverter<L, V> extends LootSerializer<L, V>, LootDeserializer<L, V> {

}
