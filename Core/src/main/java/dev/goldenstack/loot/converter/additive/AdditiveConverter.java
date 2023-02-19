package dev.goldenstack.loot.converter.additive;

import dev.goldenstack.loot.converter.LootConverter;

/**
 * A standard representation of a loot converter that serializes in an additive way, although it is possible to use it
 * as a normal serializer.
 * @param <V> the type of object that will be serialized and deserialized
 */
public interface AdditiveConverter<V> extends LootConverter<V>, AdditiveLootSerializer<V> {

}
