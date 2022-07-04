package dev.goldenstack.loot.conversion;

import com.google.gson.JsonElement;
import dev.goldenstack.loot.context.LootConversionContext;
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
    public abstract boolean canDeserialize(@Nullable JsonElement element, @NotNull LootConversionContext<L> context);

    /**
     * @param element the element to deserialize
     * @param context the context for this deserialization
     * @return the instance of {@link T} that was created
     * @throws LootParsingException if, for some reason, something goes wrong while deserializing
     */
    public abstract @NotNull T deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<L> context) throws LootParsingException;

    /**
     * @return true if this converter should be used to serialize the provided element
     */
    public abstract boolean canSerialize(@NotNull T input, @NotNull LootConversionContext<L> context);

    /**
     * @param input the input that needs to be serialized
     * @param context the context for this serialization
     * @return the JSON element that was created
     * @throws LootParsingException if, for some reason, something goes wrong while serializing
     */
    public abstract @NotNull JsonElement serialize(@NotNull T input, @NotNull LootConversionContext<L> context) throws LootParsingException;

}
