package dev.goldenstack.loot.util;

import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Represents some function, usually associated with serialization or deserialization, that can throw a serialization
 * exception.
 * @param <I> the input type of this function
 * @param <O> the output type of this function
 */
@FunctionalInterface
public interface FallibleFunction<I, O> {

    /**
     * Processes some input in some arbitrary way.
     * @param input the input provided to this function
     * @return the arbitrary output of the function
     * @throws SerializationException if there was some error while trying to perform the operation
     */
    O apply(I input) throws SerializationException;

}
