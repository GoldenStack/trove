package dev.goldenstack.loot.context;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Represents information about something that has happened that can be used to help generate loot.<br>
 */
public class LootContext {

    private Random random;
    private Instance instance;
    private int looting;
    private float luck;

    private final Map<LootContextParameter<?>, Object> parameters;

    public LootContext(){
        parameters = new HashMap<>();
    }

    /**
     * Returns the random number generator that this LootContext uses
     */
    public @Nullable Random random(){
        return random;
    }

    /**
     * Returns the instance that this LootContext uses
     */
    public @Nullable Instance instance(){
        return instance;
    }

    /**
     * Returns the looting level for this LootContext
     */
    public int looting(){
        return looting;
    }

    /**
     * Returns the luck for this LootContext
     */
    public float luck(){
        return luck;
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T> T getParameter(@NotNull LootContextParameter<T> parameter){
        return (T) this.parameters.get(parameter);
    }

    /**
     * If this LootContext has a null random number generator, it initializes it via {@code new Random();}. Then, it
     * returns the random.
     */
    public @NotNull Random findRandom(){
        if (this.random == null){
            this.random = new Random();
        }
        return this.random;
    }

    /**
     * Sets the position that this LootContext contains.<br>
     * Note that this should not affect anything outside this LootContext.
     */
    @Contract("_, _ -> this")
    public @NotNull <T> LootContext parameter(@NotNull LootContextParameter<T> parameter, T value){
        this.parameters.put(parameter, value);
        return this;
    }

    /**
     * Sets the random that this LootContext uses
     */
    @Contract("_ -> this")
    public @NotNull LootContext random(@Nullable Random random){
        this.random = random;
        return this;
    }

    /**
     * Sets the instance that this LootContext uses.<br>
     * Note that this should not affect anything outside this LootContext.
     */
    @Contract("_ -> this")
    public @NotNull LootContext instance(@Nullable Instance instance){
        this.instance = instance;
        return this;
    }

    /**
     * Sets the looting level of this LootContext.<br>
     * Note that this should not affect anything outside this LootContext.
     */
    @Contract("_ -> this")
    public @NotNull LootContext looting(int looting){
        this.looting = looting;
        return this;
    }

    /**
     * Sets the amount of luck that this LootContext uses.<br>
     * Note that this should not affect anything outside this LootContext.
     */
    @Contract("_ -> this")
    public @NotNull LootContext luck(float luck){
        this.luck = luck;
        return this;
    }

    @Override
    public String toString() {
        return "LootContext[parameters=" + parameters + ", random=" + random +
                        ", instance=" + instance + ", looting=" + looting + ", luck=" + luck + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootContext context = (LootContext) o;
        return looting == context.looting && Float.compare(context.luck, luck) == 0 &&
                Objects.equals(this.parameters, context.parameters) && Objects.equals(random, context.random) &&
                Objects.equals(instance, context.instance);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(parameters);
        result = 31 * result + Objects.hashCode(random);
        result = 31 * result + Objects.hashCode(instance);
        result = 31 * result + looting;
        result = 31 * result + (int) luck;
        return result;
    }
}