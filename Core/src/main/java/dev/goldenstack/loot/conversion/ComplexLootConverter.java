package dev.goldenstack.loot.conversion;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles complex conversion where nothing is guaranteed about provided objects.
 * @param <L> the loot item
 * @param <T> the class of the object that will be provided
 */
public abstract class ComplexLootConverter<L, T extends LootAware<L>> {

    /**
     * @return true if this converter should be used to deserialize the provided element
     */
    public abstract boolean canDeserialize(@Nullable JsonElement element, @NotNull LootConversionManager<L, T> converter);

    /**
     * @param element the element to deserialize
     * @param converter the exact converter that is requesting deserialization
     * @return the instance of {@link T} that was created
     * @throws LootParsingException if, for some reason, something goes wrong while deserializing
     */
    public abstract @NotNull T deserialize(@Nullable JsonElement element, @NotNull LootConversionManager<L, T> converter) throws LootParsingException;

    /**
     * @return true if this converter should be used to serialize the provided element
     */
    public abstract boolean canSerialize(@NotNull T input, @NotNull LootConversionManager<L, T> converter);

    /**
     * @param input the input that needs to be serialized
     * @param converter the exact converter that is requesting serialization
     * @return the JSON element that was created
     * @throws LootParsingException if, for some reason, something goes wrong while serializing
     */
    public abstract @NotNull JsonElement serialize(@NotNull T input, @NotNull LootConversionManager<L, T> converter) throws LootParsingException;

}
