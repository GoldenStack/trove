package dev.goldenstack.loot.context;

import com.google.common.collect.ImmutableSet;
import dev.goldenstack.loot.ImmuTables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import static dev.goldenstack.loot.context.LootContextParameter.*;

/**
 * A group of {@code LootParameter}s, with a set of required {@code LootParameter}s and a set of optional ones.
 * Internally, the required and optional sets are combined into an "allowed" set.
 */
public class LootParameterGroup {

        /**
         * A LootParameterGroup with no required and no optional parameters.
         */
        // Yes, this looks a bit wonky :(
    public static final @NotNull LootParameterGroup
        EMPTY = builder().build(),
        /**
         * A LootParameterGroup that represents what should be provided when a chest is opened. It includes a required
         * origin (where the chest was) and an optional entity (the entity that opened the chest).
         */
        CHEST = builder().require(ORIGIN).optional(THIS_ENTITY).build(),

        /**
         * A LootParameterGroup that represents what should be provided when a command is run. It includes a required
         * origin (where the entity or command block that ran the command was) and an optional entity (the entity that
         * ran the command).
         */
        COMMAND = builder().require(ORIGIN).optional(THIS_ENTITY).build(),

        /**
         * A LootParameterGroup that represents what should be provided when a selector is triggered. It includes a
         * required origin (where the entity or command block that triggered the selector was) and an optional entity
         * (the entity that triggered the selector).
         */
        SELECTOR = builder().require(ORIGIN).optional(THIS_ENTITY).build(),

        /**
         * A LootParameterGroup that represents what should be provided when a fishing rod is pulled in. It includes a
         * required origin (where the bobber of the fishing rod was), a required tool (the fishing rod item), and an
         * optional entity (the entity that was fishing).
         */
        FISHING = builder().require(ORIGIN).require(TOOL).optional(THIS_ENTITY).build(),

        /**
         * A LootParameterGroup that represents what should be provided when an entity is killed or something otherwise
         * happens. It includes a required entity (the entity), a required origin (where the entity was), an optional
         * killer entity (the entity that killed it, such as the player who shot an arrow), an optional direct killer (
         * the entity who actually killed it, such as the arrow that did the damage), and an optional last player damage
         * (the last damage that was dealt to the entity).
         */
        ENTITY = builder().require(THIS_ENTITY).require(ORIGIN).require(DAMAGE_SOURCE).optional(KILLER_ENTITY).optional(DIRECT_KILLER_ENTITY).optional(LAST_DAMAGE_PLAYER).build(),
        /**
         * A LootParameterGroup that represents when a villager gives a gift to a player. It requires the origin (the
         * location of the villager) and the required entity (the villager that is giving the gift).
         */
        GIFT = builder().require(ORIGIN).require(THIS_ENTITY).build(),
        /**
         * A LootParameterGroup that represents when a piglin is bartering. It requires the entity (the piglin that is
         * doing the bartering).
         */
        BARTER = builder().require(THIS_ENTITY).build(),
        /**
         * A LootParameterGroup that represents when an entity gets advancement rewards. It requires the entity (the
         * entity that is getting the rewards) and a required origin (the position of the entity).
         */
        ADVANCEMENT_REWARD = builder().require(THIS_ENTITY).require(ORIGIN).build(),
        /**
         * A LootParameterGroup that represents when an advancement is granted to a target entity. It requires an entity
         * (the entity that is getting the advancement) and a required origin (the location of the entity that is granting the
         * advancement). The documentation for this may be unreliable.
         */
        ADVANCEMENT_ENTITY = builder().require(THIS_ENTITY).require(ORIGIN).build(),
        /**
         * A LootParameterGroup that requires everything. This is not done dynamically, so if you add your own parameters
         * you will need to create a new group with the extra ones.
         */
        GENERIC = builder().require(THIS_ENTITY).require(LAST_DAMAGE_PLAYER).require(DAMAGE_SOURCE).require(KILLER_ENTITY).require(DIRECT_KILLER_ENTITY).require(ORIGIN).require(BLOCK_STATE).require(BLOCK_ENTITY).require(TOOL).require(EXPLOSION_RADIUS).build(),
        /**
         * A LootParameterGroup that represents when something happens to a block. It requires the block state (the state
         * of the block when it was broken), a required origin (the location of the block), a required tool (the tool that
         * broke the block or affected it in some way), an optional entity (the entity that caused whatever is happening),
         * an optional block entity (the entity that the block was), and an optional explosion radius (the radius of the
         * explosion that caused whatever happened).
         */
        BLOCK = builder().require(BLOCK_STATE).require(ORIGIN).require(TOOL).optional(THIS_ENTITY).optional(BLOCK_ENTITY).optional(EXPLOSION_RADIUS).build();

    /**
     * Adds the default parameter groups to the provided ImmuTables instance.
     */
    public static void addDefaults(@NotNull ImmuTables loader) {
        var map = loader.getLootParameterGroupRegistry();
        map.put("minecraft:empty", EMPTY);
        map.put("minecraft:chest", CHEST);
        map.put("minecraft:command", COMMAND);
        map.put("minecraft:selector", SELECTOR);
        map.put("minecraft:fishing", FISHING);
        map.put("minecraft:entity", ENTITY);
        map.put("minecraft:gift", GIFT);
        map.put("minecraft:barter", BARTER);
        map.put("minecraft:advancement_reward", ADVANCEMENT_REWARD);
        map.put("minecraft:advancement_entity", ADVANCEMENT_ENTITY);
        map.put("minecraft:generic", GENERIC);
        map.put("minecraft:block", BLOCK);
    }

    private final @NotNull ImmutableSet<LootContextParameter<?>> required, allowed;

    /**
     * Creates a new LootParameterGroup with the provided required and optional parameters.
     */
    public LootParameterGroup(@NotNull Set<LootContextParameter<?>> required, @NotNull Set<LootContextParameter<?>> optional) {
        this.required = ImmutableSet.copyOf(required);
        this.allowed = ImmutableSet.<LootContextParameter<?>>builder().addAll(required).addAll(optional).build();
    }

    /**
     * Utility method to register this group with the provided key in the loader.
     */
    public void register(@NotNull String key, @NotNull ImmuTables loader) {
        loader.getLootParameterGroupRegistry().put(key, this);
    }

    /**
     * Returns the set of required parameters.
     */
    public @NotNull ImmutableSet<LootContextParameter<?>> required() {
        return required;
    }

    /**
     * Returns the set of allowed parameters.
     */
    public @NotNull ImmutableSet<LootContextParameter<?>> allowed() {
        return allowed;
    }

    /**
     * Returns true if the provided parameter is required.
     */
    public boolean isRequired(@NotNull LootContextParameter<?> parameter) {
        return this.required.contains(parameter);
    }

    /**
     * Returns true if the provided parameter is allowed.
     */
    public boolean isAllowed(@NotNull LootContextParameter<?> parameter) {
        return this.allowed.contains(parameter);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");

        for (LootContextParameter<?> parameter : this.allowed) {
            joiner.add((this.required.contains(parameter) ? "!" : "") + parameter.key().asString());
        }

        return "LootParameterGroup[" + joiner + "]";
    }

    /**
     * Returns a new builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Utility class for building LootParameterGroup instances
     */
    public static class Builder {
        private final Set<LootContextParameter<?>> required = new HashSet<>();
        private final Set<LootContextParameter<?>> optional = new HashSet<>();

        private Builder() {}

        /**
         * Makes the provided parameter required. The parameter cannot already be required and cannot already be optional.
         */
        @Contract("_ -> this")
        public @NotNull Builder require(@NotNull LootContextParameter<?> parameter) {
            if (this.optional.contains(parameter)) {
                throw new IllegalArgumentException("Parameter \"" + parameter.key() + "\" is already optional for this builder!");
            }
            if (!this.required.add(parameter)) {
                throw new IllegalArgumentException("Parameter \"" + parameter.key() + "\" is already required for this builder!");
            }
            return this;
        }

        /**
         * Makes the provided parameter optional. The parameter cannot already be optional and cannot already be required.
         */
        @Contract("_ -> this")
        public @NotNull Builder optional(@NotNull LootContextParameter<?> parameter) {
            if (this.required.contains(parameter)){
                throw new IllegalArgumentException("Parameter \"" + parameter.key() + "\" is already required for this builder!");
            }
            if (!this.optional.add(parameter)){
                throw new IllegalArgumentException("Parameter \"" + parameter.key() + "\" is already optional for this builder!");
            }
            return this;
        }

        /**
         * Builds a new LootParameterGroup from this instance. This can be used multiple times safely, if you wanted to
         * for some reason.
         */
        public @NotNull LootParameterGroup build() {
            return new LootParameterGroup(required, optional);
        }
    }
}
