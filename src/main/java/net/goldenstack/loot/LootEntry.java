package net.goldenstack.loot;

import net.goldenstack.loot.util.Serial;
import net.goldenstack.loot.util.Template;
import net.goldenstack.loot.util.VanillaInterface;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry in a loot table that can generate a list of {@link Choice choices} that each have their own loot and weight.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootEntry {

    @NotNull BinaryTagSerializer<LootEntry> SERIALIZER = Template.registry("type",
            Template.entry("empty", Empty.class, Empty.SERIALIZER),
            Template.entry("item", Item.class, Item.SERIALIZER),
            Template.entry("loot_table", LootTable.class, LootTable.SERIALIZER),
            Template.entry("dynamic", Dynamic.class, Dynamic.SERIALIZER),
            Template.entry("tag", Tag.class, Tag.SERIALIZER),
            Template.entry("alternatives", Alternatives.class, Alternatives.SERIALIZER),
            Template.entry("sequence", Sequence.class, Sequence.SERIALIZER),
            Template.entry("group", Group.class, Group.SERIALIZER)
    );

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
         * where the quality is multiplied by the provided context's luck ({@link LootContext#LUCK}).
         */
        interface Standard extends Choice {

            /**
             * The weight of this choice. When calculating the final weight, this value is simply added to the result.
             * @return the base weight of this choice
             */
            @Range(from = 1L, to = Long.MAX_VALUE) long weight();

            /**
             * The quality of the choice. When calculating the final weight, this number is multiplied by the context's luck
             * value, which is stored at the key {@link LootContext#LUCK}.
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
    
    record Alternatives(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {

        public static final @NotNull BinaryTagSerializer<Alternatives> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Alternatives::predicates,
                "children", Serial.lazy(() -> LootEntry.SERIALIZER).list().optional(List.of()), Alternatives::children,
                Alternatives::new
        );

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

        public static final @NotNull BinaryTagSerializer<Sequence> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Sequence::predicates,
                "children", Serial.lazy(() -> LootEntry.SERIALIZER).list().optional(List.of()), Sequence::children,
                Sequence::new
        );

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

        public static final @NotNull BinaryTagSerializer<Group> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Group::predicates,
                "children", Serial.lazy(() -> LootEntry.SERIALIZER).list().optional(List.of()), Group::children,
                Group::new
        );

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
                long weight, long quality, @NotNull Material name) implements Choice.Single {

        public static final @NotNull BinaryTagSerializer<Item> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Item::predicates,
                "functions", LootFunction.SERIALIZER.list().optional(List.of()), Item::functions,
                "weight", Serial.LONG.optional(1L), Item::weight,
                "quality", Serial.LONG.optional(0L), Item::quality,
                "name", Material.NBT_TYPE, Item::name,
                Item::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            return List.of(LootFunction.apply(functions, ItemStack.of(name), context));
        }
    }

    record Dynamic(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                long weight, long quality, @NotNull NamespaceID name) implements Choice.Single {

        public static final @NotNull BinaryTagSerializer<Dynamic> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Dynamic::predicates,
                "functions", LootFunction.SERIALIZER.list().optional(List.of()), Dynamic::functions,
                "weight", Serial.LONG.optional(1L), Dynamic::weight,
                "quality", Serial.LONG.optional(0L), Dynamic::quality,
                "name", Serial.KEY, Dynamic::name,
                Dynamic::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);
            if (block == null) return List.of();

            return switch (name.asString()) {
                case "minecraft:sherds" -> {
                    List<ItemStack> items = new ArrayList<>();
                    for (Material material : block.getTag(VanillaInterface.DECORATED_POT_SHERDS)) {
                        items.add(ItemStack.of(material));
                    }
                    yield items;
                }
                case "minecraft:contents" -> block.getTag(VanillaInterface.CONTAINER_ITEMS);
                default -> List.of();
            };
        }
    }

    record Empty(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                long weight, long quality) implements Choice.Single {

        public static final @NotNull BinaryTagSerializer<Empty> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Empty::predicates,
                "functions", LootFunction.SERIALIZER.list().optional(List.of()), Empty::functions,
                "weight", Serial.LONG.optional(1L), Empty::weight,
                "quality", Serial.LONG.optional(0L), Empty::quality,
                Empty::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            return List.of();
        }
    }

    record LootTable(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                     long weight, long quality, @NotNull NamespaceID value) implements Choice.Single {

        public static final @NotNull BinaryTagSerializer<LootTable> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), LootTable::predicates,
                "functions", LootFunction.SERIALIZER.list().optional(List.of()), LootTable::functions,
                "weight", Serial.LONG.optional(1L), LootTable::weight,
                "quality", Serial.LONG.optional(0L), LootTable::quality,
                "value", Serial.KEY, LootTable::value,
                LootTable::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            var table = context.vanilla().tableRegistry(value);
            if (table == null) return List.of();

            return LootFunction.apply(functions, table.generate(context), context);
        }
    }

    record Tag(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
               long weight, long quality, @NotNull net.minestom.server.gamedata.tags.Tag name, boolean expand) implements Choice.Single {

        public static final @NotNull BinaryTagSerializer<Tag> SERIALIZER = Template.template(
                "conditions", LootPredicate.SERIALIZER.list().optional(List.of()), Tag::predicates,
                "functions", LootFunction.SERIALIZER.list().optional(List.of()), Tag::functions,
                "weight", Serial.LONG.optional(1L), Tag::weight,
                "quality", Serial.LONG.optional(0L), Tag::quality,
                "name", Serial.tag(net.minestom.server.gamedata.tags.Tag.BasicType.ITEMS), Tag::name,
                "expand", BinaryTagSerializer.BOOLEAN, Tag::expand,
                Tag::new
        );

        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) {
                return List.of();
            } else if (!expand) {
                return List.of(this);
            }

            List<Choice> choices = new ArrayList<>();
            for (var key : name.getValues()) {
                Material material = Material.fromNamespaceId(key);
                if (material == null) continue;
                choices.add(new Choice() {
                    @Override
                    public @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context) {
                        return Tag.this.getWeight(context);
                    }

                    @Override
                    public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
                        return List.of(ItemStack.of(material));
                    }

                });
            }
            return choices;
        }

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            List<ItemStack> items = new ArrayList<>();
            for (var key : name.getValues()) {
                Material material = Material.fromNamespaceId(key);
                if (material == null) continue;

                items.add(LootFunction.apply(functions, ItemStack.of(material), context));
            }

            return items;
        }
    }


}
