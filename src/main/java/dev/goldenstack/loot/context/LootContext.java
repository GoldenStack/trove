package dev.goldenstack.loot.context;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

/**
 * Represents information about something that has happened that can be used to help generate loot.<br>
 * For example, if a player opens a chest, it might have {@code pos} and {@code luck} initialized, and if a player kills
 * a zombie, it might have {@code pos}, {@code killed}, {@code killer}, {@code looting}, {@code instance}, and
 * {@code luck} initialized.
 */
public class LootContext {

    private Pos pos;
    private Entity killed, killer;
    private Random random;
    private Instance instance;
    private int looting;
    private float luck;

    public LootContext(){}

    /**
     * Returns the position of whatever triggered this LootContext
     */
    public @Nullable Pos pos(){
        return pos;
    }

    /**
     * Returns the entity that was killed for this LootContext
     */
    public @Nullable Entity killed(){
        return killed;
    }

    /**
     * Returns the entity that acted as the killer for this LootContext
     */
    public @Nullable Entity killer(){
        return killer;
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
    @Contract("_ -> this")
    public @NotNull LootContext pos(@Nullable Pos pos){
        this.pos = pos;
        return this;
    }

    /**
     * Sets the entity that was killed.<br>
     * Note that this should not affect anything outside this LootContext.
     */
    @Contract("_ -> this")
    public @NotNull LootContext killed(@Nullable Entity killed){
        this.killed = killed;
        return this;
    }

    /**
     * Sets the entity that killed something.<br>
     * Note that this should not affect anything outside this LootContext.
     */
    @Contract("_ -> this")
    public @NotNull LootContext killer(@Nullable Entity killer){
        this.killer = killer;
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
        return "LootContext[pos=" + pos +
                        ", killed=" + killed +
                        ", killer=" + killer +
                        ", random=" + random +
                        ", instance=" + instance +
                        ", looting=" + looting +
                        ", luck=" + luck +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootContext context = (LootContext) o;
        return looting == context.looting && Float.compare(context.luck, luck) == 0 && Objects.equals(pos, context.pos) && Objects.equals(killed, context.killed) && Objects.equals(killer, context.killer) && Objects.equals(random, context.random) && Objects.equals(instance, context.instance);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(pos);
        result = 31 * result + Objects.hashCode(killed);
        result = 31 * result + Objects.hashCode(killer);
        result = 31 * result + Objects.hashCode(random);
        result = 31 * result + Objects.hashCode(instance);
        result = 31 * result + looting;
        result = 31 * result + (int) luck;
        return result;
    }
}