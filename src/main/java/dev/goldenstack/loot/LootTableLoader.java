package dev.goldenstack.loot;

import dev.goldenstack.loot.json.JsonSerializationManager;
import dev.goldenstack.loot.provider.number.NumberProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LootTableLoader {

    private final @NotNull JsonSerializationManager<NumberProvider> numberProviderManager;
    private LootTableLoader(@NotNull Builder builder){
        JsonSerializationManager.Builder<NumberProvider> numberProviderBuilder = JsonSerializationManager.builder();
        builder.numberProviderBuilder.accept(numberProviderBuilder);
        this.numberProviderManager = numberProviderBuilder.owner(this).build();
    }

    public @NotNull JsonSerializationManager<NumberProvider> getNumberProviderManager() {
        return numberProviderManager;
    }

    public static @NotNull Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private Builder(){}

        private Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder;

        @Contract("_ -> this")
        @NotNull Builder numberProviderBuilder(@NotNull Consumer<JsonSerializationManager.Builder<NumberProvider>> numberProviderBuilder){
            this.numberProviderBuilder = numberProviderBuilder;
            return this;
        }

        public @NotNull LootTableLoader build(){
            return new LootTableLoader(this);
        }

    }
}
