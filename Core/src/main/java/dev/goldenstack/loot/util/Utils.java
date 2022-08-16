package dev.goldenstack.loot.util;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.LootDeserializer;
import dev.goldenstack.loot.converter.LootSerializer;
import dev.goldenstack.loot.converter.meta.AdditiveLootSerializer;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootEntry;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private Utils() {}

    /**
     * Serialzies the provided items into a list of {@link I}.
     * @param items the list of items to serialize
     * @param serializer the serializer that will be fed items
     * @param context the context, to feed into the serializer
     * @return a node with the value equal to the serialized list of items
     * @param <L> the loot item type
     * @param <I> the input type
     * @throws ConfigurateException if something goes wrong while serializing
     */
    public static <L, I> @NotNull ConfigurationNode serializeList(@NotNull List<I> items,
                                                                  @NotNull LootSerializer<L, I> serializer,
                                                                  @NotNull LootConversionContext<L> context) throws ConfigurateException {
        var node = context.loader().createNode();
        if (items.isEmpty()) {
            return node; // Returns a node with no value
        }
        List<ConfigurationNode> listChildren = new ArrayList<>();
        for (var item : items) {
            listChildren.add(serializer.serialize(item, context));
        }
        return node.setList(ConfigurationNode.class, listChildren);
    }

    /**
     * Deserializes the provided node into a list of {@link O}. If the returned list is empty, it may be immutable.
     * @param input the node to deserialize
     * @param deserializer the deserializer that will be fed nodes
     * @param context the context, to feed into the serializer
     * @return a list containing the items that were deserialized from the node
     * @param <L> the loot item type
     * @param <O> the output type
     * @throws ConfigurateException if something goes wrong while serializing or if the provided node has a value but
     *                              it's not a list of configuration nodes
     */
    public static <L, O> @NotNull List<O> deserializeList(@NotNull ConfigurationNode input,
                                                          @NotNull LootDeserializer<L, O> deserializer,
                                                          @NotNull LootConversionContext<L> context) throws ConfigurateException {
        if (input.empty()) {
            return List.of();
        }
        List<O> output = new ArrayList<>();
        List<ConfigurationNode> children = input.getList(ConfigurationNode.class);
        if (children == null) {
            throw new ConfigurateException(input, "Expected the value of the node to be a list of configuration nodes");
        }
        for (var child : children) {
            output.add(deserializer.deserialize(child, context));
        }
        return output;
    }

    /**
     * Generates loot from the provided entries. This process is repeated {@code rolls} times. For each of the provided
     * entries, {@link LootEntry#requestOptions(LootGenerationContext) LootEntry#requestOptions} is only called once and
     * {@link LootEntry.Option#getWeight(LootGenerationContext) LootEntry.Option#getWeight} is only called once, so it
     * is theoretically safe for them to return different results even if the context is the same.<br>
     * To be specific, for each roll, the entries are consolidated into options via #requestOptions, a random option
     * from them is determined via each option's weight, and that option is used to generate loot.<br>
     * This is in the core library because, although it's not the only way to generate loot, it's a pretty
     * straightforward way and will usually be the method used.
     * @param entries the entries to generate for
     * @param rolls the number of times to generate loot from the entries
     * @param context the context object, to use if required
     * @return the generated list of loot items
     * @param <L> the loot item type
     */
    public static <L> @NotNull List<L> generateStandardLoot(@NotNull List<LootEntry<L>> entries, long rolls, @NotNull LootGenerationContext context) {
        List<L> items = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            // Weight and choices must be recalculated each time as their results theoretically may change
            List<LootEntry.Option<L>> options = new ArrayList<>();
            for (LootEntry<L> entry : entries) {
                options.addAll(entry.requestOptions(context));
            }

            if (options.isEmpty()) {
                continue;
            }

            long totalWeight = 0;
            long[] lowerWeightMilestones = new long[options.size()];
            for (int j = 0; j < options.size(); j++) {
                lowerWeightMilestones[j] = totalWeight;
                // Prevent the weight of this option from being less than 1
                totalWeight += Math.max(1, options.get(j).getWeight(context));
            }

            long value = context.random().nextLong(0, totalWeight);

            LootEntry.Option<L> option = options.get(options.size() - 1);

            for (int j = 0; j < lowerWeightMilestones.length; j++) {
                if (value >= lowerWeightMilestones[j]) {
                    option = options.get(j);
                    break;
                }
            }

            items.addAll(option.generate(context));
        }
        return items;
    }

    /**
     * Assures that the value of the provided node is an instance of {@link T}. This is different from
     * {@link ConfigurationNode#require(Class)} because it throws an exception with the provided node as context,
     * therefore having more accurate error messages.
     * @param node the node to get the value of
     * @param type the type to assure
     * @return the guaranteed instance of {@code T}
     * @param <T> the type to get
     * @throws ConfigurateException if the node's value was not an instance of {@link T}
     */
    public static <T> @NotNull T require(@NotNull ConfigurationNode node, @NotNull Class<T> type) throws ConfigurateException {
        T result = node.get(type);
        if (result == null) {
            throw new ConfigurateException(node, "Expected a value of type '" + type + "'");
        }
        return result;
    }

    /**
     * Creates a new keyed loot converter out of the provided information. This just exists to reduce boilerplate code.
     * @param id the string identifier of the created converter
     * @param type the type token representing the converted type
     * @param serializer the converter's serializer
     * @param deserializer the converter's deserializer
     * @return a new keyed loot converter based on the provided information
     * @param <L> the loot item type
     * @param <V> the converted type
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <L, V> @NotNull KeyedLootConverter<L, V> createKeyedConverter(@NotNull String id, @NotNull TypeToken<V> type,
                                                                                @NotNull AdditiveLootSerializer<L, V> serializer,
                                                                                @NotNull LootDeserializer<L, V> deserializer) {
        return new KeyedLootConverter<>(id, type) {
            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext<L> context) throws ConfigurateException {
                serializer.serialize(input, result, context);
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
                return deserializer.deserialize(input, context);
            }
        };
    }

    /**
     * Creates a new loot converter out of the provided serializer and deserializer. This just exists to reduce
     * boilerplate code.
     * @param serializer the converter's serializer
     * @param deserializer the converter's deserializer
     * @return a new loot converter based on the provided serializer and deserializer
     * @param <L> the loot item type
     * @param <V> the converted type
     */
    public static <L, V> @NotNull LootConverter<L, V> createConverter(@NotNull LootSerializer<L, V> serializer,
                                                                      @NotNull LootDeserializer<L, V> deserializer) {
        return new LootConverter<>() {
            @Override
            public @NotNull ConfigurationNode serialize(@NotNull V input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
                return serializer.serialize(input, context);
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext<L> context) throws ConfigurateException {
                return deserializer.deserialize(input, context);
            }
        };
    }
}
