package dev.goldenstack.loot.converter;

/**
 * A standard representation of something that can convert a type of object ({@link V}) to and from a configuration
 * node. This is a simple combination of {@link LootSerializer} and {@link LootDeserializer}.
 * @param <V> the type of object that will be serialized and deserialized
 */
public interface LootConverter<V> extends LootSerializer<V>, LootDeserializer<V> {

}
