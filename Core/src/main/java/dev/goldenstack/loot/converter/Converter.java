package dev.goldenstack.loot.converter;

/**
 * A standard representation of something that can convert a type of object ({@link V}) to and from a configuration
 * node. This is a simple combination of {@link Serializer} and {@link Deserializer}.
 * @param <L> the loot item type
 * @param <V> the type of object that will be serialized and deserialized
 */
public interface Converter<L, V> extends Serializer<L, V>, Deserializer<L, V> {

}
