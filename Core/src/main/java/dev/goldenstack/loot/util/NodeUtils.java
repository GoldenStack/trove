package dev.goldenstack.loot.util;

import dev.goldenstack.loot.context.LootConversionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * General ConfigurationNode-related utilities.
 */
public class NodeUtils {

    private NodeUtils() {}

    /**
     * @param items the items to serialize
     * @param serializer the interface that will be fed the items
     * @param context the context, to use for creating nodes and feeding to the serializer
     * @return a node containing a list of the serialized items
     * @param <L> the loot item
     * @param <I> the input class
     * @throws ConfigurateException if the items could not be serialized
     */
    public static <L, I> @NotNull ConfigurationNode serializeList(@Nullable Collection<I> items,
                                                                  @NotNull Serializer<L, I> serializer,
                                                                  @NotNull LootConversionContext<L> context) throws ConfigurateException {
        ConfigurationNode node = context.loader().createNode();
        if (items == null || items.isEmpty()) {
            return node;
        }
        List<ConfigurationNode> input = new ArrayList<>();
        for (I item : items) {
            input.add(serializer.serialize(item, context));
        }
        return node.setList(ConfigurationNode.class, input);
    }

    /**
     * @param node the node to deserialize
     * @param deserializer the interface that will be fed the child nodes
     * @param context the context, to feed to the deserializer
     * @return a list containing all of the deserialized items from the node
     * @param <L> the loot item
     * @param <O> the output class
     * @throws ConfigurateException if the node could not be deserialized into a list
     */
    public static <L, O> @NotNull List<O> deserializeList(@NotNull ConfigurationNode node,
                                                          @NotNull Deserializer<L, O> deserializer,
                                                          @NotNull LootConversionContext<L> context) throws ConfigurateException {
        if (node.empty()) {
            return List.of();
        }
        List<O> output = new ArrayList<>();
        List<ConfigurationNode> children = node.getList(ConfigurationNode.class);
        if (children == null) {
            throw new ConfigurateException(node, "Expected the value of the node to be a list of nodes");
        }
        for (var child : children) {
            output.add(deserializer.deserialize(child, context));
        }
        return output;
    }

    @FunctionalInterface
    public interface Serializer<L, I> {
        @NotNull ConfigurationNode serialize(@NotNull I input, @NotNull LootConversionContext<L> context) throws ConfigurateException;
    }

    @FunctionalInterface
    public interface Deserializer<L, O> {
        @NotNull O deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<L> context) throws ConfigurateException;
    }
}
