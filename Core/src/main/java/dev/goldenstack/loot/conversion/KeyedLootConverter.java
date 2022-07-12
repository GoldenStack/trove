package dev.goldenstack.loot.conversion;

import dev.goldenstack.loot.context.LootConversionContext;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Handles serialization and deserialization ("conversion") for whatever {@code <T>} is. This implementation stores the
 * {@link TypeToken<T>} object in order for any managers this is registered in to detect which converter to use without
 * relying on the object itself to provide it, theoretically allowing for objects that do not have a converter to be
 * used in loot tables, although this is generally not a good idea.
 * @param <L> the loot item
 * @param <T> the class that can be converted
 */
public abstract class KeyedLootConverter<L, T extends LootAware<L>> {

    private final @NotNull String key;
    private final @NotNull TypeToken<T> typeToken;

    /**
     * Creates a new loot converter with the provided information.
     * @param key the key (basically the ID) of the objects that will be converted
     * @param typeToken the type token representing which type of object will be converted
     */
    public KeyedLootConverter(@NotNull String key, @NotNull TypeToken<T> typeToken) {
        this.key = key;
        this.typeToken = typeToken;
    }

    /**
     * @return the key of this converter
     */
    public final @NotNull String key() {
        return key;
    }

    /**
     * @return the type token of the type that this will convert
     */
    public TypeToken<T> typeToken() {
        return typeToken;
    }

    /**
     * Deserializes the provided configuration node into an instance of {@code T}. Although the node is mutable, it's
     * not a good idea to modify fields on it without knowing specifically where its source is.
     * @param node the configuration node that should be deserialized
     * @param context the context, to use for any other required information for deserialization
     * @return the instance of {@code T} that was deserialized
     * @throws ConfigurateException if, for some reason, something goes wrong while deserializing
     */
    public abstract @NotNull T deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException;

    /**
     * Serializes the provided instance of {@code T} onto the provided configuration node. The node will usually already
     * have one field set (the key, which is probably used for deserialization), so overwriting that key should be
     * avoided. However, it's likely that the specific location of the key will be known, so it shouldn't be difficult
     * to avoid overwriting it. Additionally, it's not a good idea to rely on the state of the node anyway.
     * @param input the input object that will be serialized
     * @param result the configuration node that should have fields added to it when serializing
     * @param context the context, to use for any other required information for serialization
     * @throws ConfigurateException if, for some reason, something goes wrong while serializing
     */
    public abstract void serialize(@NotNull T input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<L> context) throws ConfigurateException;
}
