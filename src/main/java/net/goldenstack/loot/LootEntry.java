package net.goldenstack.loot;

import net.goldenstack.loot.util.VanillaInterface;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry in a loot table that can generate a list of {@link Choice choices} that each have their own loot and weight.
 */
public interface LootEntry {

    /**
     * Generates any number of possible choices to choose from when generating loot.
     * @param context the context object, to use if required
     * @return a list, with undetermined mutability, containing the options that were generated
     */
    @NotNull List<Choice> requestChoices(@NotNull LootContext context);

    /**
     * A choice, generated from an entry, that could potentially be chosen.
     */
    interface Choice extends LootGenerator {

        /**
         * Calculates the weight of this choice, to be used when choosing which choices should be used.
         * This number should not be below 1.<br>
         * When using the result of this method, be aware of the fact that it's valid for implementations of this method
         * to return different values even when the provided context is the identical.
         * @param context the context object, to use if required
         * @return the weight of this choice
         */
        @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context);

        
        /**
         * A choice that uses the standard method of generating weight - adding the {@link #weight()} to the {@link #quality()}
         * where the quality is multiplied by the provided context's luck ({@link LootContextKeys#LUCK}).
         */
        interface Standard extends Choice {

            /**
             * The weight of this choice. When calculating the final weight, this value is simply added to the result.
             * @return the base weight of this choice
             */
            @Range(from = 1L, to = Long.MAX_VALUE) long weight();

            /**
             * The quality of the choice. When calculating the final weight, this number is multiplied by the context's luck
             * value, which is stored at the key {@link LootContextKeys#LUCK}.
             * @return the quality of the choice
             */
            @Range(from = 0L, to = Long.MAX_VALUE) long quality();

            @Override
            default @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context) {
                return Math.max(1, (long) Math.floor(weight() + quality() * context.get(LootContext.LUCK, 0d)));
            }

        }

        /**
         * A standard single choice entry that only returns itself when its conditions all succeed.
         */
        interface Single extends LootEntry, LootEntry.Choice, Standard {

            /**
             * @return this choice's predicates
             */
            @NotNull List<LootPredicate> predicates();

            /**
             * Requests choices, returning none if {@link #predicates()} are all true.
             * {@inheritDoc}
             */
            @Override
            default @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
                return LootPredicate.all(predicates(), context) ? List.of(this) : List.of();
            }

        }

    }
    
    record Alternative(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {
        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return List.of();

            for (var entry : this.children()) {
                var options = entry.requestChoices(context);
                if (!options.isEmpty()) {
                    return options;
                }
            }
            return List.of();
        }
    }

    record Sequence(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {
        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return List.of();

            List<Choice> options = new ArrayList<>();
            for (var entry : this.children()) {
                var choices = entry.requestChoices(context);
                if (choices.isEmpty()) {
                    break;
                }
                options.addAll(choices);
            }
            return options;
        }
    }
    
    record Group(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {
        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return List.of();

            List<Choice> choices = new ArrayList<>();
            for (var entry : this.children()) {
                choices.addAll(entry.requestChoices(context));
            }
            return choices;
        }
    }

    record Item(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                long weight, long quality, @NotNull Material material) implements Choice.Single {

        @Override
        public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
            return List.of(LootFunction.apply(functions, ItemStack.of(material), context));
        }
    }

    record Dynamic(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                long weight, long quality, @NotNull NamespaceID choiceID) implements Choice.Single {

        @SuppressWarnings("DataFlowIssue")
        @Override
        public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);
            if (block == null) return List.of();

            CompoundBinaryTag nbt = block.hasNbt() ? block.nbt() : CompoundBinaryTag.empty();

            VanillaInterface vanilla = context.require(LootContext.VANILLA_INTERFACE);
            return vanilla.getDynamicDrops(choiceID, nbt);
        }
    }

    record Empty(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                long weight, long quality) implements Choice.Single {

        @Override
        public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
            return List.of();
        }
    }

    record Table(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                 long weight, long quality, @NotNull NamespaceID tableID) implements Choice.Single {
        @Override
        public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
            var tables = context.get(LootContext.REGISTERED_TABLES);
            if (tables == null) return List.of();

            LootTable table = tables.apply(tableID);
            if (table == null) return List.of();

            return LootFunction.apply(functions, table.apply(context), context);
        }
    }

    record Tag(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
               long weight, long quality, @NotNull net.minestom.server.gamedata.tags.Tag tag, boolean expand) implements Choice.Single {

        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) {
                return List.of();
            } else if (!expand) {
                return List.of(this);
            }

            List<Choice> choices = new ArrayList<>();
            for (var key : tag.getValues()) {
                Material material = Material.fromNamespaceId(key);
                if (material == null) continue;
                choices.add(new Choice() {
                    @Override
                    public @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context) {
                        return Tag.this.getWeight(context);
                    }

                    @Override
                    public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
                        return List.of(ItemStack.of(material));
                    }

                });
            }
            return choices;
        }

        @Override
        public @NotNull List<ItemStack> apply(@NotNull LootContext context) {
            List<ItemStack> items = new ArrayList<>();
            for (var key : tag.getValues()) {
                Material material = Material.fromNamespaceId(key);
                if (material == null) continue;

                items.add(LootFunction.apply(functions, ItemStack.of(material), context));
            }

            return items;
        }
    }


}
