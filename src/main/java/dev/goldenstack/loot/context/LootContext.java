package dev.goldenstack.loot.context;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

/**
 * Represents information about something that has happened that can be used to help generate loot.<br>
 * For example, if a player opens a chest, it might have {@code pos} and {@code luck} initialized, and if a player kills
 * a zombie, it might have {@code pos}, {@code killed}, {@code killer}, {@code looting}, and {@code luck} initialized.
 */
public class LootContext {

    private Pos pos;
    private Entity killed, killer;
    private Random random;
    private int looting;
    private float luck;
    public LootContext(@Nullable Pos pos, @Nullable Entity killed, @Nullable Entity killer, @Nullable Random random, int looting, float luck){
        this.pos = pos;
        this.killed = killed;
        this.killer = killer;
        this.random = random;
        this.looting = looting;
        this.luck = luck;
    }

    @Override
    public String toString() {
        return "LootContext[pos=" + pos + ", killed=" + killed + ", killer=" + killer +
                ", random=" + random + ", looting=" + looting + ", luck=" + luck + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootContext that = (LootContext) o;
        return looting == that.looting && Float.compare(that.luck, luck) == 0 && Objects.equals(pos, that.pos) &&
                Objects.equals(killed, that.killed) && Objects.equals(killer, that.killer) &&
                Objects.equals(random, that.random);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(this.pos);
        result = 31 * result + Objects.hashCode(this.killed);
        result = 31 * result + Objects.hashCode(this.killer);
        result = 31 * result + Objects.hashCode(this.random);
        result = 31 * result + looting;
        result = 31 * result + (int) luck;
        return result;
    }

    /**
     * @return This context's current position
     */
    public @Nullable Pos pos(){
        return pos;
    }

    /**
     * Sets the context's position to the provided {@code Pos} instance.<br>
     * Note that this should not affect anything outside of the loot table.
     */
    public void pos(@Nullable Pos pos){
        this.pos = pos;
    }

    /**
     * @return The entity that was killed
     */
    public @Nullable Entity killed(){
        return killed;
    }

    /**
     * Sets the entity that was killed to the provided {@code Entity}.<br>
     * Note that this should not affect anything outside of the loot table.
     */
    public void killed(@Nullable Entity killed){
        this.killed = killed;
    }

    /**
     * @return The entity that killed something
     */
    public @Nullable Entity killer(){
        return killer;
    }

    /**
     * Sets the entity that was the killer to the provided {@code Entity} instance.<br>
     * Note that this should not affect anything outside of the loot table.
     */
    public void killer(@Nullable Entity killer){
        this.killer = killer;
    }

    /**
     * @return This context's {@code Random}
     */
    public @Nullable Random random(){
        return random;
    }

    /**
     * Initializes this context's random if its current random is null, and then returns this instance's random.<br>
     * This is here in case there is ever a different way to generate a random for a LootContext instance, but also
     * because it can help shorten other code.
     */
    public @NotNull Random findRandom(){
        if (this.random == null){
            this.random = new Random();
        }
        return this.random;
    }

    /**
     * Sets this context's {@code Random} to the {@code random}
     */
    public void random(@Nullable Random random){
        this.random = random;
    }

    /**
     * @return The {@code looting} value of this context
     */
    public int looting(){
        return looting;
    }

    /**
     * Sets this context's {@code looting} value
     */
    public void looting(int looting){
        this.looting = looting;
    }

    /**
     * @return The {@code luck} value of this context
     */
    public float luck(){
        return luck;
    }

    /**
     * Sets this context's {@code luck} value
     */
    public void luck(float luck){
        this.luck = luck;
    }

    public static @NotNull Builder builder(){
        return new Builder();
    }

    /**
     * Utility class for building LootContext instances
     */
    public static final class Builder {
        private Pos pos = null;
        private Entity killed = null, killer = null;
        private int looting = 0;
        private float luck = 0;
        private Random random = null;

        private Builder(){}

        /**
         * Sets the position of this builder
         * @param pos The position
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder pos(@Nullable Pos pos) {
            this.pos = pos;
            return this;
        }

        /**
         * Sets the entity that was killed for this builder
         * @param killed The entity that was killed
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder killed(@Nullable Entity killed) {
            this.killed = killed;
            return this;
        }

        /**
         * Sets this builder's killer
         * @param killer The entity's killer
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder killer(@Nullable Entity killer) {
            this.killer = killer;
            return this;
        }

        /**
         * Sets this builder's looting value
         * @param looting The looting value
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder looting(int looting) {
            this.looting = looting;
            return this;
        }

        /**
         * Sets this builder's luck
         * @param luck The luck value
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder luck(float luck) {
            this.luck = luck;
            return this;
        }

        /**
         * Sets this builder's random to a new random
         * @param random The random
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder random(@Nullable Random random) {
            this.random = random;
            return this;
        }

        /**
         * Sets this builder's random to a new random with the provided seed
         * @param seed The random seed
         * @return This, for chaining
         */
        @Contract("_ -> this")
        public @NotNull Builder random(long seed) {
            this.random = new Random(seed);
            return this;
        }

        /**
         * Builds this builder into a new LootContext instance.
         * Any unset values are initialized as null or zero.
         * @return The built LootContext
         */
        public @NotNull LootContext build() {
            return new LootContext(pos, killed, killer, random, looting, luck);
        }
    }

}