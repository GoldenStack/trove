package dev.goldenstack.loot.minestom.context;

import dev.goldenstack.loot.context.LootContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static dev.goldenstack.loot.minestom.context.LootContextKeys.*;

/**
 * A group of context keys. The expected keys are required, while the permitted keys are the only keys that the context
 * is allowed to have.
 * @param expected the set of keys that are required in all contexts
 * @param permitted the set of keys that are allowed in all contexts. An empty set means that all are allowed.
 */
public record LootContextKeyGroup(@NotNull Set<LootContext.Key<?>> expected, @NotNull Set<LootContext.Key<?>> permitted) {

    /**
     * A LootContextKeyGroup with no required and no optional keys.
     */
    public static final @NotNull LootContextKeyGroup
        EMPTY = builder().build(),
        /**
         * A LootContextKeyGroup that represents what should be provided when a chest is opened. It includes a required
         * origin (where the chest was) and an optional entity (the entity that opened the chest).
         */
        CHEST = builder().expect(ORIGIN).permit(THIS_ENTITY).build(),

        /**
         * A LootContextKeyGroup that represents what should be provided when a command is run. It includes a required
         * origin (where the entity or command block that ran the command was) and an optional entity (the entity that
         * ran the command).
         */
        COMMAND = builder().expect(ORIGIN).permit(THIS_ENTITY).build(),

        /**
         * A LootContextKeyGroup that represents what should be provided when a selector is triggered. It includes a
         * required origin (where the entity or command block that triggered the selector was) and an optional entity
         * (the entity that triggered the selector).
         */
        SELECTOR = builder().expect(ORIGIN).permit(THIS_ENTITY).build(),

        /**
         * A LootContextKeyGroup that represents what should be provided when a fishing rod is pulled in. It includes a
         * required origin (where the bobber of the fishing rod was), a required tool (the fishing rod item), and an
         * optional entity (the entity that was fishing).
         */
        FISHING = builder().expect(ORIGIN, TOOL).permit(THIS_ENTITY).build(),

        /**
         * A LootContextKeyGroup that represents what should be provided when an entity is killed or something otherwise
         * happens. It includes a required entity (the entity), a required origin (where the entity was), an optional
         * killer entity (the entity that killed it, such as the player who shot an arrow), an optional direct killer (
         * the entity who actually killed it, such as the arrow that did the damage), and an optional last player damage
         * (the last damage that was dealt to the entity).
         */
        ENTITY = builder().expect(THIS_ENTITY, ORIGIN, DAMAGE_SOURCE).permit(KILLER_ENTITY, DIRECT_KILLER_ENTITY, LAST_DAMAGE_PLAYER).build(),

        /**
         * A LootContextKeyGroup that represents when a villager gives a gift to a player. It requires the origin (the
         * location of the villager) and the required entity (the villager that is giving the gift).
         */
        GIFT = builder().expect(ORIGIN, THIS_ENTITY).build(),

        /**
         * A LootContextKeyGroup that represents when a piglin is bartering. It requires the entity (the piglin that is
         * doing the bartering).
         */
        BARTER = builder().expect(THIS_ENTITY).build(),

        /**
         * A LootContextKeyGroup that represents when an entity gets advancement rewards. It requires the entity (the
         * entity that is getting the rewards) and a required origin (the position of the entity).
         */
        ADVANCEMENT_REWARD = builder().expect(THIS_ENTITY, ORIGIN).build(),

        /**
         * A LootContextKeyGroup that represents when an advancement is granted to a target entity. It requires an entity
         * (the entity that is getting the advancement) and a required origin (the location of the entity that is granting the
         * advancement). The documentation for this may be unreliable.
         */
        ADVANCEMENT_ENTITY = builder().expect(THIS_ENTITY, ORIGIN).build(),

        /**
         * A LootContextKeyGroup that requires everything. This is not done dynamically, so if you add your own keys
         * you will need to create a new group with the extra ones.
         */
        GENERIC = builder().expect(THIS_ENTITY, LAST_DAMAGE_PLAYER, DAMAGE_SOURCE, KILLER_ENTITY, DIRECT_KILLER_ENTITY, ORIGIN, BLOCK_STATE, BLOCK_ENTITY, TOOL, EXPLOSION_RADIUS).build(),

        /**
         * A LootContextKeyGroup that represents when something happens to a block. It requires the block state (the state
         * of the block when it was broken), a required origin (the location of the block), a required tool (the tool that
         * broke the block or affected it in some way), an optional entity (the entity that caused whatever is happening),
         * an optional block entity (the entity that the block was), and an optional explosion radius (the radius of the
         * explosion that caused whatever happened).
         */
        BLOCK = builder().expect(BLOCK_STATE, ORIGIN, TOOL).permit(THIS_ENTITY, BLOCK_ENTITY, EXPLOSION_RADIUS).build();

    public LootContextKeyGroup {
        expected = Set.copyOf(expected);

        Set<LootContext.Key<?>> fullPermitted = new HashSet<>(permitted);
        fullPermitted.addAll(expected);
        permitted = Set.copyOf(fullPermitted);
    }

    /**
     * Assures that all of the expected keys are in the provided loot context and that the only keys in the provided
     * context are permitted. The result is returned as a boolean.
     * @param context the context to test the information of
     * @return true if the context is valid according to this group
     */
    public boolean verify(@NotNull LootContext context) {
        var information = context.information();
        for (var key : expected) {
            if (!information.containsKey(key)) {
                return false;
            }
        }
        for (var key : information.keySet()) {
            if (!permitted.contains(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Assures that all of the expected keys are in the provided loot context and that the only keys in the provided
     * context are permitted. If this fails, an exception that explains why is thrown.
     * @param context the context to test the information of
     */
    public void assureVerified(@NotNull LootContext context) {
        var information = context.information();
        for (var key : expected) {
            if (!information.containsKey(key)) {
                throw new IllegalArgumentException("Provided context does not have key '" + key.key() + "'");
            }
        }
        for (var key : information.keySet()) {
            if (!permitted.contains(key)) {
                throw new IllegalArgumentException("Provided context has key '" + key + "' but it is not allowed");
            }
        }
    }

    /**
     * Creates a new builder for this class, with no information and a null loader.<br>
     * Note: the returned builder is not thread-safe, concurrent, or synchronized in any way.
     * @return a new LootContextKeyGroup builder
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final @NotNull Set<LootContext.Key<?>> expected = new HashSet<>();
        private final @NotNull Set<LootContext.Key<?>> permitted = new HashSet<>();

        private Builder() {}

        @Contract("_ -> this")
        public @NotNull Builder expect(@NotNull LootContext.Key<?> @NotNull ... expected) {
            this.expected.addAll(Arrays.asList(expected));
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder permit(@NotNull LootContext.Key<?> @NotNull ... permitted) {
            this.permitted.addAll(Arrays.asList(permitted));
            return this;
        }

        @Contract(" -> new")
        public @NotNull LootContextKeyGroup build() {
            return new LootContextKeyGroup(expected, permitted);
        }
    }

}
