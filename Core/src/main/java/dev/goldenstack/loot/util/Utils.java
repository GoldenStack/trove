package dev.goldenstack.loot.util;

import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.LootDeserializer;
import dev.goldenstack.loot.converter.LootSerializer;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.converter.additive.AdditiveLootSerializer;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.structure.LootEntry;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Utils {
    private Utils() {}

    /**
     * Generates loot from the provided entries. This process is repeated {@code rolls} times. For each of the provided
     * entries, {@link LootEntry#requestChoices(LootGenerationContext) LootEntry#requestOptions} is only called once and
     * {@link LootEntry.Choice#getWeight(LootGenerationContext) LootEntry.Choice#getWeight} is only called once, so it
     * is theoretically safe for them to return different results even if the context is the same.<br>
     * To be specific, for each roll, the entries are consolidated into options via #requestOptions, a random choice
     * from them is determined via each choice's weight, and that choice is used to generate loot.<br>
     * This is in the core library because, although it's not the only way to generate loot, it's a pretty
     * straightforward way and will usually be the method used.
     * @param entries the entries to generate for
     * @param rolls the number of times to generate loot from the entries
     * @param context the context object, to use if required
     * @return the generated list of loot items
     */
    public static @NotNull LootBatch generateStandardLoot(@NotNull List<LootEntry> entries, long rolls, @NotNull LootGenerationContext context) {
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            // Weight and choices must be recalculated each time as their results theoretically may change
            List<LootEntry.Choice> choices = new ArrayList<>();
            for (LootEntry entry : entries) {
                choices.addAll(entry.requestChoices(context));
            }

            if (choices.isEmpty()) {
                continue;
            }

            long totalWeight = 0;
            long[] lowerWeightMilestones = new long[choices.size()];
            for (int j = 0; j < choices.size(); j++) {
                lowerWeightMilestones[j] = totalWeight;
                // Prevent the weight of this choice from being less than 1
                totalWeight += Math.max(1, choices.get(j).getWeight(context));
            }

            long value = context.random().nextLong(0, totalWeight);

            LootEntry.Choice choice = choices.get(choices.size() - 1);

            for (int j = 0; j < lowerWeightMilestones.length; j++) {
                if (value >= lowerWeightMilestones[j]) {
                    choice = choices.get(j);
                    break;
                }
            }

            items.addAll(choice.generate(context).items());
        }
        return new LootBatch(items);
    }

    /**
     * Creates a new keyed loot converter out of the provided information. This just exists to reduce boilerplate code.
     * @param id the string identifier of the created converter
     * @param type the type token representing the converted type
     * @param serializer the converter's serializer
     * @param deserializer the converter's deserializer
     * @return a new keyed loot converter based on the provided information
     * @param <V> the converted type
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <V> @NotNull KeyedLootConverter<V> createKeyedConverter(@NotNull String id, @NotNull TypeToken<V> type,
                                                                          @NotNull AdditiveLootSerializer<V> serializer,
                                                                          @NotNull LootDeserializer<V> deserializer) {
        return new KeyedLootConverter<>(id, type) {
            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
                serializer.serialize(input, result, context);
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
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
     * @param <V> the converted type
     */
    public static <V> @NotNull LootConverter<V> createConverter(@NotNull LootSerializer<V> serializer,
                                                                @NotNull LootDeserializer<V> deserializer) {
        return new LootConverter<>() {
            @Override
            public @NotNull ConfigurationNode serialize(@NotNull V input, @NotNull LootConversionContext context) throws ConfigurateException {
                return serializer.serialize(input, context);
            }

            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
                return deserializer.deserialize(input, context);
            }
        };
    }

    /**
     * Generates a new additive converter that merges the provided components of one.
     * @param serializer the new converter's serializer
     * @param deserializer the new converter's deserializer
     * @return a new additive converter based on the provided serializer and deserializer
     * @param <V> the converted type
     */
    public static <V> @NotNull AdditiveConverter<V> createAdditive(@NotNull AdditiveLootSerializer<V> serializer,
                                                                   @NotNull LootDeserializer<V> deserializer) {
        return new AdditiveConverter<>() {
            @Override
            public @NotNull V deserialize(@NotNull ConfigurationNode input, @NotNull LootConversionContext context) throws ConfigurateException {
                return deserializer.deserialize(input, context);
            }

            @Override
            public void serialize(@NotNull V input, @NotNull ConfigurationNode result, @NotNull LootConversionContext context) throws ConfigurateException {
                serializer.serialize(input, result, context);
            }
        };
    }

    /**
     * Creates an additive converter proxied by the converter returned by {@code converterFinder}.
     * @param converterFinder the function that gets the additive converter
     * @return a new additive converter that uses the finder to determine which one it is proxying
     * @param <V> the converted type
     */
    public static <V> @NotNull AdditiveConverter<V> additiveFromContext(@NotNull Function<LootConversionContext, AdditiveConverter<V>> converterFinder) {
        return createAdditive(
                (input, result, context) -> converterFinder.apply(context).serialize(input, result, context),
                (input, context) -> converterFinder.apply(context).deserialize(input, context)
        );
    }

}
