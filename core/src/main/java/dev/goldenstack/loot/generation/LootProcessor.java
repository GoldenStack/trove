package dev.goldenstack.loot.generation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents some black box that can process loot.
 */
public interface LootProcessor extends Consumer<@NotNull Object> {

    /**
     * Creates a processor that handles exclusively
     * @param type the type to handle
     * @param consumer the processor for instances of the type
     * @return a loot processor that handles only the provided type
     * @param <T> the type to process
     */
    static <T> @NotNull LootProcessor processClass(@NotNull Class<T> type, @NotNull Consumer<T> consumer) {
        return LootProcessor.builder().processClass(type, consumer).build();
    }

    /**
     * Creates a new builder for multi-type loot processors.
     * <br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new builder
     */
    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Generic builder class for multi-type loot processors.
     */
    class Builder {
        private final @NotNull List<IndividualProcessor> processors = new ArrayList<>();

        private Builder() {}

        /**
         * Processes objects with the consumer if they are the of the provided type.
         * @param type the class to process
         * @param consumer the processor for instances of the class
         * @return this, for chaining
         * @param <T> the class to process
         */
        @SuppressWarnings("unchecked")
        @Contract("_, _ -> this")
        public <T> @NotNull Builder processClass(@NotNull Class<T> type, @NotNull Consumer<T> consumer) {
            processors.add(new IndividualProcessor(type::isInstance, o -> consumer.accept((T) o)));
            return this;
        }

        /**
         * Processes objects with the consumer according to the predicate.
         * @param predicate the predicate that determines whether or not each object is processed
         * @param consumer the consumer that will be used
         * @return this, for chaining
         */
        @Contract("_, _ -> this")
        public @NotNull Builder process(@NotNull Predicate<Object> predicate, @NotNull Consumer<Object> consumer) {
            processors.add(new IndividualProcessor(predicate, consumer));
            return this;
        }

        /**
         * Builds a LootProcessor from this builder.
         * @return a LootProcessor from this builder
         */
        @Contract(" -> new")
        public @NotNull LootProcessor build() {
            return new LootProcessorImpl(this.processors);
        }

    }

}

record IndividualProcessor(@NotNull Predicate<Object> canProcess, @NotNull Consumer<Object> processor) {}

record LootProcessorImpl(@NotNull List<IndividualProcessor> processors) implements LootProcessor {

    LootProcessorImpl {
        processors = List.copyOf(processors);
    }

    @Override
    public void accept(@NotNull Object o) {
        for (var processor : processors) {
            if (processor.canProcess().test(o)) {
                processor.processor().accept(o);
                return;
            }
        }

        throw new IllegalArgumentException("Cannot process result '" + o + "'");
    }
}