package net.goldenstack.loot;

import net.goldenstack.loot.util.*;
import net.goldenstack.loot.util.nbt.NBTPath;
import net.goldenstack.loot.util.nbt.NBTReference;
import net.goldenstack.loot.util.nbt.NBTUtils;
import net.goldenstack.loot.util.predicate.ItemPredicate;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.item.component.*;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A function that allows loot to pass through it, potentially making modifications.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootFunction {

    @NotNull BinaryTagSerializer<LootFunction> SERIALIZER = Template.compoundSplit(
            Serial.lazy(() -> LootFunction.SERIALIZER).list().map(Sequence::new, Sequence::functions),
            Template.registry("function",
                    Template.entry("sequence", Sequence.class, Sequence.SERIALIZER),
                    Template.entry("filtered", Filtered.class, Filtered.SERIALIZER),
                    Template.entry("set_potion", SetPotion.class, SetPotion.SERIALIZER),
                    Template.entry("explosion_decay", ExplosionDecay.class, ExplosionDecay.SERIALIZER),
                    Template.entry("reference", Reference.class, Reference.SERIALIZER),
                    Template.entry("apply_bonus", ApplyBonus.class, ApplyBonus.SERIALIZER),
                    Template.entry("copy_name", CopyName.class, CopyName.SERIALIZER),
                    Template.entry("toggle_tooltips", ToggleTooltips.class, ToggleTooltips.SERIALIZER),
                    Template.entry("set_stew_effect", SetStewEffect.class, SetStewEffect.SERIALIZER),
                    Template.entry("set_ominous_bottle_amplifier", SetOminousBottleAmplifier.class, SetOminousBottleAmplifier.SERIALIZER),
                    Template.entry("copy_custom_data", CopyCustomData.class, CopyCustomData.SERIALIZER),
                    Template.entry("limit_count", LimitCount.class, LimitCount.SERIALIZER),
                    Template.entry("set_count", SetCount.class, SetCount.SERIALIZER),
                    Template.entry("set_item", SetItem.class, SetItem.SERIALIZER),
                    Template.entry("set_loot_table", SetLootTable.class, SetLootTable.SERIALIZER),
                    Template.entry("copy_components", CopyComponents.class, CopyComponents.SERIALIZER),
                    Template.entry("copy_state", CopyState.class, CopyState.SERIALIZER),
                    Template.entry("enchanted_count_increase", EnchantedCountIncrease.class, EnchantedCountIncrease.SERIALIZER),
                    Template.entry("set_custom_data", SetCustomData.class, SetCustomData.SERIALIZER),
                    Template.entry("set_custom_model_data", SetCustomModelData.class, SetCustomModelData.SERIALIZER),
                    Template.entry("set_damage", SetDamage.class, SetDamage.SERIALIZER),
                    Template.entry("set_enchantments", SetEnchantments.class, SetEnchantments.SERIALIZER),
                    Template.entry("enchant_with_levels", EnchantWithLevels.class, EnchantWithLevels.SERIALIZER),
                    Template.entry("set_book_cover", SetBookCover.class, SetBookCover.SERIALIZER),
                    Template.entry("fill_player_head", FillPlayerHead.class, FillPlayerHead.SERIALIZER),
                    Template.entry("enchant_randomly", EnchantRandomly.class, EnchantRandomly.SERIALIZER),
                    Template.entry("furnace_smelt", FurnaceSmelt.class, FurnaceSmelt.SERIALIZER),
                    Template.entry("exploration_map", ExplorationMap.class, ExplorationMap.SERIALIZER),
                    Template.entry("set_name", SetName.class, SetName.SERIALIZER),
                    Template.entry("set_instrument", SetInstrument.class, SetInstrument.SERIALIZER),
                    Template.entry("set_attributes", SetAttributes.class, SetAttributes.SERIALIZER)
            )
    );

    /**
     * Performs any mutations on the provided object and returns the result.
     * @param input the input item to this function
     * @param context the context object, to use if required
     * @return the modified form of the input
     */
    @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context);
    
    /**
     * Applies each function to the given item consecutively.
     * @param functions the functions to apply
     * @param item the item to modify
     * @param context the context to use
     * @return the modified item
     */
    static @NotNull ItemStack apply(@NotNull Collection<LootFunction> functions, @NotNull ItemStack item, @NotNull LootContext context) {
        for (LootFunction function : functions) {
            item = function.apply(item, context);
        }
        return item;
    }

    /**
     * Applies each function to each of the given items consecutively.
     * @param functions the functions to apply
     * @param items the items to modify
     * @param context the context to use
     * @return the modified items
     */
    static @NotNull List<ItemStack> apply(@NotNull Collection<LootFunction> functions, @NotNull List<ItemStack> items, @NotNull LootContext context) {
        List<ItemStack> newItems = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            newItems.add(LootFunction.apply(functions, item, context));
        }
        return newItems;
    }

    record Sequence(@NotNull List<LootFunction> functions) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<Sequence> SERIALIZER = Template.template(
                "functions", Serial.lazy(() -> LootFunction.SERIALIZER).list(), Sequence::functions,
                Sequence::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            return LootFunction.apply(functions, input, context);
        }
    }

    record Filtered(@NotNull List<LootPredicate> predicates, @NotNull ItemPredicate predicate, @NotNull LootFunction modifier) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<Filtered> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), Filtered::predicates,
                "item_filter", Serial.lazy(ItemPredicate.SERIALIZER::get), Filtered::predicate,
                "modifier", Serial.lazy(() -> LootFunction.SERIALIZER), Filtered::modifier,
                Filtered::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            return LootPredicate.all(predicates, context) && predicate.test(input) ?
                    modifier.apply(input, context) : input;
        }
    }

    record SetPotion(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID id) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetPotion> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetPotion::predicates,
                "id", Serial.KEY, SetPotion::id,
                SetPotion::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (id.asString().equals("minecraft:empty")) {
                return input.without(ItemComponent.POTION_CONTENTS);
            }

            PotionContents existing = input.get(ItemComponent.POTION_CONTENTS, PotionContents.EMPTY);
            PotionContents updated = new PotionContents(PotionType.fromNamespaceId(id), existing.customColor(), existing.customEffects());

            return input.with(ItemComponent.POTION_CONTENTS, updated);
        }
    }

    record ExplosionDecay(@NotNull List<LootPredicate> predicates) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<ExplosionDecay> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), ExplosionDecay::predicates,
                ExplosionDecay::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            if (radius == null) return input;

            Random random = context.require(LootContext.RANDOM);

            float chance = 1 / radius;
            int trials = input.amount();

            int newAmount = 0;

            for (int i = 0; i < trials; i++) {
                if (random.nextFloat() <= chance) {
                    newAmount++;
                }
            }

            return input.withAmount(newAmount);
        }
    }

    record Reference(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID name) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<Reference> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), Reference::predicates,
                "name", Serial.KEY, Reference::name,
                Reference::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            LootFunction function = context.vanilla().functionRegistry(name);

            return function != null ? function.apply(input, context) : input;
        }
    }

    record ApplyBonus(@NotNull List<LootPredicate> predicates, @NotNull DynamicRegistry.Key<Enchantment> enchantment, @NotNull Formula formula) implements LootFunction {

        private static final @NotNull BinaryTagSerializer<List<LootPredicate>> PREDICATES = Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of());
        private static final @NotNull BinaryTagSerializer<DynamicRegistry.Key<Enchantment>> KEY = Serial.key();

        public static final @NotNull BinaryTagSerializer<ApplyBonus> SERIALIZER = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull ApplyBonus value) {
                CompoundBinaryTag.Builder nbt = CompoundBinaryTag.builder();
                nbt.put("conditions", PREDICATES.write(context, value.predicates()));
                nbt.put("enchantment", KEY.write(context, value.enchantment()));

                return (switch (value.formula()) {
                    case Formula.UniformBonusCount uniform -> nbt
                            .put("formula", StringBinaryTag.stringBinaryTag("minecraft:uniform_bonus_count"))
                            .put("parameters", Formula.UniformBonusCount.SERIALIZER.write(context, uniform));
                    case Formula.OreDrops drops -> nbt
                            .put("formula", StringBinaryTag.stringBinaryTag("minecraft:ore_drops"))
                            .put("parameters", Formula.OreDrops.SERIALIZER.write(context, drops));
                    case Formula.BinomialWithBonusCount binomial -> nbt
                            .put("formula", StringBinaryTag.stringBinaryTag("minecraft:binomial_with_bonus_count"))
                            .put("parameters", Formula.BinomialWithBonusCount.SERIALIZER.write(context, binomial));
                }).build();
            }

            @SuppressWarnings("DataFlowIssue")
            @Override
            public @NotNull ApplyBonus read(@NotNull Context context, @NotNull BinaryTag raw) {
                if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");

                List<LootPredicate> predicates = PREDICATES.read(context, tag.get("conditions"));
                DynamicRegistry.Key<Enchantment> enchantment = KEY.read(context, tag.get("enchantments"));

                String type = BinaryTagSerializer.STRING.read(context, tag.get("formula"));
                BinaryTag parameters = tag.get("parameters");

                Formula formula = switch (type) {
                    case "minecraft:uniform_bonus_count" -> Formula.UniformBonusCount.SERIALIZER.read(context, parameters);
                    case "minecraft:ore_drops" -> Formula.OreDrops.SERIALIZER.read(context, parameters);
                    case "minecraft:binomial_with_bonus_count" -> Formula.BinomialWithBonusCount.SERIALIZER.read(context, parameters);
                    default -> throw new IllegalArgumentException("Invalid formula '" + type + "'");
                };

                return new ApplyBonus(predicates, enchantment, formula);
            }
        };

        public sealed interface Formula {

            int calculate(@NotNull Random random, int count, int level);

            record UniformBonusCount(int bonusMultiplier) implements Formula {

                public static final @NotNull BinaryTagSerializer<UniformBonusCount> SERIALIZER = Template.template(
                        "bonusMultiplier", BinaryTagSerializer.INT, UniformBonusCount::bonusMultiplier,
                        UniformBonusCount::new
                );

                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    return count + random.nextInt(bonusMultiplier * level + 1);
                }
            }

            record OreDrops() implements Formula {

                public static final @NotNull BinaryTagSerializer<OreDrops> SERIALIZER = Template.template(
                    OreDrops::new
                );

                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    if (level <= 0) return count;

                    return count * Math.min(1, random.nextInt(level + 2));
                }
            }

            record BinomialWithBonusCount(float probability, int extra) implements Formula {

                public static final @NotNull BinaryTagSerializer<BinomialWithBonusCount> SERIALIZER = Template.template(
                        "probability", BinaryTagSerializer.FLOAT, BinomialWithBonusCount::probability,
                        "extra", BinaryTagSerializer.INT, BinomialWithBonusCount::extra,
                        BinomialWithBonusCount::new
                );

                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    for (int i = 0; i < extra + level; i++) {
                        if (random.nextFloat() < probability) {
                            count++;
                        }
                    }

                    return count;
                }
            }

        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);
            if (tool == null) return input;

            int level = EnchantmentUtils.level(tool, enchantment);
            int newCount = formula.calculate(context.require(LootContext.RANDOM), input.amount(), level);

            return input.withAmount(newCount);
        }
    }

    record CopyName(@NotNull List<LootPredicate> predicates, @NotNull RelevantTarget source) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<CopyName> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), CopyName::predicates,
                "source", RelevantTarget.SERIALIZER, CopyName::source,
                CopyName::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Object key = context.get(source.key());

            Component customName;
            if (key instanceof Entity entity && entity.getCustomName() != null) {
                customName = entity.getCustomName();
            } else if (key instanceof Block block && block.hasTag(VanillaInterface.CUSTOM_NAME)) {
                customName = block.getTag(VanillaInterface.CUSTOM_NAME);
            } else {
                return input;
            }

            return input.with(ItemComponent.CUSTOM_NAME, customName);
        }
    }

    record ToggleTooltips(@NotNull List<LootPredicate> predicates, @NotNull Map<ComponentToggler<?>, Boolean> source) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<ToggleTooltips> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), ToggleTooltips::predicates,
                "source", Serial.map(
                        ComponentToggler.TOGGLERS.stream().collect(Collectors.toMap(t -> t.data().name(), Function.identity()))::get,
                        t -> t.data().name(),
                        BinaryTagSerializer.BOOLEAN
                ), ToggleTooltips::source,
                ToggleTooltips::new
        );

        public record ComponentToggler<T>(@NotNull DataComponent<T> data, @NotNull BiFunction<T, Boolean, T> toggler) {

            public static final List<ComponentToggler<?>> TOGGLERS = List.of(
                    new ComponentToggler<>(ItemComponent.TRIM, (trim, shown) -> new ArmorTrim(trim.material(), trim.pattern(), shown)),
                    new ComponentToggler<>(ItemComponent.DYED_COLOR, (color, shown) -> new DyedItemColor(color.color(), shown)),
                    new ComponentToggler<>(ItemComponent.ENCHANTMENTS, (list, shown) -> new EnchantmentList(list.enchantments(), shown)),
                    new ComponentToggler<>(ItemComponent.STORED_ENCHANTMENTS, (list, shown) -> new EnchantmentList(list.enchantments(), shown)),
                    new ComponentToggler<>(ItemComponent.UNBREAKABLE, (unbreakable, shown) -> new Unbreakable(shown)),
                    new ComponentToggler<>(ItemComponent.CAN_BREAK, (preds, shown) -> new BlockPredicates(preds.predicates(), shown)),
                    new ComponentToggler<>(ItemComponent.CAN_PLACE_ON, (preds, shown) -> new BlockPredicates(preds.predicates(), shown)),
                    new ComponentToggler<>(ItemComponent.ATTRIBUTE_MODIFIERS, (mods, shown) -> new AttributeList(mods.modifiers(), shown)),
                    new ComponentToggler<>(ItemComponent.JUKEBOX_PLAYABLE, (playable, shown) -> new JukeboxPlayable(playable.song(), shown))
            );

            public @NotNull ItemStack apply(@NotNull ItemStack input, boolean shown) {
                T component = input.get(data);
                if (component == null) return input;

                return input.with(data, toggler.apply(component, shown));
            }
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            for (var toggle : source.entrySet()) {
                input = toggle.getKey().apply(input, toggle.getValue());
            }

            return input;
        }
    }

    record SetStewEffect(@NotNull List<LootPredicate> predicates, @NotNull List<AddedEffect> effects) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetStewEffect> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetStewEffect::predicates,
                "effects", AddedEffect.SERIALIZER.list(), SetStewEffect::effects,
                SetStewEffect::new
        );

        public record AddedEffect(@NotNull PotionEffect effect, @NotNull LootNumber duration) {

            public static final @NotNull BinaryTagSerializer<AddedEffect> SERIALIZER = Template.template(
                    "type", Serial.KEY.map(PotionEffect::fromNamespaceId, PotionEffect::namespace), AddedEffect::effect,
                    "duration", LootNumber.SERIALIZER, AddedEffect::duration,
                    AddedEffect::new
            );

        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (!Material.SUSPICIOUS_STEW.equals(input.material()) || effects.isEmpty()) return input;

            AddedEffect effect = effects.get(context.require(LootContext.RANDOM).nextInt(effects.size()));

            long duration = effect.duration().getInt(context);
            if (!effect.effect().registry().isInstantaneous()) {
                duration *= ServerFlag.SERVER_TICKS_PER_SECOND;
            }

            SuspiciousStewEffects.Effect added = new SuspiciousStewEffects.Effect(effect.effect(), (int) duration);

            SuspiciousStewEffects current = input.get(ItemComponent.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
            return input.with(ItemComponent.SUSPICIOUS_STEW_EFFECTS, current.with(added));
        }
    }

    record SetOminousBottleAmplifier(@NotNull List<LootPredicate> predicates, @NotNull LootNumber amplifier) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetOminousBottleAmplifier> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetOminousBottleAmplifier::predicates,
                "amplifier", LootNumber.SERIALIZER, SetOminousBottleAmplifier::amplifier,
                SetOminousBottleAmplifier::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            int amplifier = Math.max(0, Math.min(this.amplifier.getInt(context), 4));

            return input.with(ItemComponent.OMINOUS_BOTTLE_AMPLIFIER, amplifier);
        }
    }

    record CopyCustomData(@NotNull List<LootPredicate> predicates, @NotNull LootNBT source, @NotNull List<Operation> ops) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<CopyCustomData> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), CopyCustomData::predicates,
                "source", LootNBT.SERIALIZER, CopyCustomData::source,
                "ops", Operation.SERIALIZER.list(), CopyCustomData::ops,
                CopyCustomData::new
        );

        public record Operation(@NotNull NBTPath source, @NotNull NBTPath target, @NotNull Operator op) {

            public static final @NotNull BinaryTagSerializer<Operation> SERIALIZER = Template.template(
                    "source", NBTPath.SERIALIZER, Operation::source,
                    "target", NBTPath.SERIALIZER, Operation::target,
                    "op", Operator.SERIALIZER, Operation::op,
                    Operation::new
            );

            public void execute(@NotNull NBTReference nbt, @NotNull BinaryTag sourceTag) {
                List<BinaryTag> nbts = new ArrayList<>();
                source.get(sourceTag).forEach(ref -> nbts.add(ref.get()));

                if (nbts.isEmpty()) return;
                op.merge(nbt, target, nbts);
            }
        }

        public enum Operator {
            REPLACE() {
                @Override
                public void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source) {
                    target.set(nbt, source.getLast());
                }
            },
            Append() {
                @Override
                public void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source) {
                    List<NBTReference> nbts = target.getWithDefaults(nbt, ListBinaryTag::empty);

                    for (var ref : nbts) {
                        source.forEach(ref::listAdd);
                    }
                }
            },
            Merge() {
                @Override
                public void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source) {
                    List<NBTReference> nbts = target.getWithDefaults(nbt, CompoundBinaryTag::empty);

                    for (var ref : nbts) {
                        if (ref.get() instanceof CompoundBinaryTag compound) {
                            for (var nbt2 : source) {
                                if (nbt2 instanceof CompoundBinaryTag compound2) {
                                    ref.set(NBTUtils.merge(compound, compound2));
                                }
                            }
                        }
                    }
                }
            };

            public static final @NotNull BinaryTagSerializer<Operator> SERIALIZER = Template.constant(op -> op.name().toLowerCase(), Operator.values());

            public abstract void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source);
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            BinaryTag sourceNBT = source.getNBT(context);
            if (sourceNBT == null) return input;

            NBTReference targetNBT = NBTReference.of(input.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).nbt());

            for (Operation operation : ops) {
                operation.execute(targetNBT, sourceNBT);
            }

            if (targetNBT.get() instanceof CompoundBinaryTag compound) {
                return input.with(ItemComponent.CUSTOM_DATA, new CustomData(compound));
            } else {
                return input;
            }
        }
    }

    record LimitCount(@NotNull List<LootPredicate> predicates, @NotNull LootNumberRange limit) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<LimitCount> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), LimitCount::predicates,
                "limit", LootNumberRange.SERIALIZER, LimitCount::limit,
                LimitCount::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            return input.withAmount(i -> (int) limit.limit(context, i));
        }
    }

    record SetCount(@NotNull List<LootPredicate> predicates, @NotNull LootNumber count, boolean add) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetCount> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetCount::predicates,
                "count", LootNumber.SERIALIZER, SetCount::count,
                "add", BinaryTagSerializer.BOOLEAN.optional(false), SetCount::add,
                SetCount::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            return input.withAmount(amount -> (this.add ? amount : 0) + this.count.getInt(context));
        }
    }

    record SetItem(@NotNull List<LootPredicate> predicates, @NotNull Material item) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetItem> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetItem::predicates,
                "item", Material.NBT_TYPE, SetItem::item,
                SetItem::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return input.builder().material(item).build();
        }
    }

    record SetLootTable(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID name, long seed) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetLootTable> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetLootTable::predicates,
                "name", Serial.KEY, SetLootTable::name,
                "seed", Serial.LONG.optional(0L), SetLootTable::seed,
                SetLootTable::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            if (input.isAir()) return input;

            return input.with(ItemComponent.CONTAINER_LOOT, new SeededContainerLoot(name.asString(), seed));
        }
    }

    record CopyComponents(@NotNull List<LootPredicate> predicates, @NotNull RelevantTarget source,
                          @Nullable List<DataComponent<?>> include, @Nullable List<DataComponent<?>> exclude) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<CopyComponents> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), CopyComponents::predicates,
                "source", RelevantTarget.SERIALIZER, CopyComponents::source,
                "include", Serial.KEY.<DataComponent<?>>map(ItemComponent::fromNamespaceId, DataComponent::namespace).list().optional(), CopyComponents::include,
                "exclude", Serial.KEY.<DataComponent<?>>map(ItemComponent::fromNamespaceId, DataComponent::namespace).list().optional(), CopyComponents::exclude,
                CopyComponents::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            throw new UnsupportedOperationException("TODO: Implement Tag<DataComponentMap> for blocks.");
        }
    }

    record CopyState(@NotNull List<LootPredicate> predicates, @NotNull Block block, @NotNull List<String> properties) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<CopyState> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), CopyState::predicates,
                "block", Serial.KEY.map(Block::fromNamespaceId, Block::namespace), CopyState::block,
                "properties", BinaryTagSerializer.STRING.list(), CopyState::properties,
                CopyState::new
        );

        public CopyState {
            List<String> props = new ArrayList<>(properties);
            props.removeIf(name -> !block.properties().containsKey(name));
            properties = List.copyOf(props);
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Block block = context.get(LootContext.BLOCK_STATE);
            if (block == null) return input;

            ItemBlockState irritableBowelSyndrome = input.get(ItemComponent.BLOCK_STATE, ItemBlockState.EMPTY);

            if (!block.key().equals(this.block.key())) return input;

            for (var prop : properties) {
                @Nullable String value = block.getProperty(prop);
                if (value == null) continue;

                irritableBowelSyndrome = irritableBowelSyndrome.with(prop, value);
            }

            return input.with(ItemComponent.BLOCK_STATE, irritableBowelSyndrome);
        }
    }

    record EnchantedCountIncrease(@NotNull List<LootPredicate> predicates, @NotNull DynamicRegistry.Key<Enchantment> enchantment,
                                  @NotNull LootNumber count, @Nullable Integer limit) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<EnchantedCountIncrease> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), EnchantedCountIncrease::predicates,
                "enchantment", Serial.key(), EnchantedCountIncrease::enchantment,
                "count", LootNumber.SERIALIZER, EnchantedCountIncrease::count,
                "limit", BinaryTagSerializer.INT.optional(), EnchantedCountIncrease::limit,
                EnchantedCountIncrease::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Entity attacker = context.get(LootContext.ATTACKING_ENTITY);
            int level = EnchantmentUtils.level(attacker, enchantment);

            if (level == 0) return input;

            int newAmount = input.amount() + level * count.getInt(context);

            return input.withAmount(limit != null ? Math.min(limit, newAmount) : newAmount);
        }
    }

    record SetCustomData(@NotNull List<LootPredicate> predicates, @NotNull CompoundBinaryTag tag) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetCustomData> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetCustomData::predicates,
                "tag", BinaryTagSerializer.STRING.map(s -> {
                    try {
                        return TagStringIO.get().asCompound(s);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, TagStringIOExt::writeTag), SetCustomData::tag,
                SetCustomData::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return input.with(ItemComponent.CUSTOM_DATA, new CustomData(tag));
        }
    }

    record SetCustomModelData(@NotNull List<LootPredicate> predicates, @NotNull LootNumber value) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetCustomModelData> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetCustomModelData::predicates,
                "value", LootNumber.SERIALIZER, SetCustomModelData::value,
                SetCustomModelData::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return input.with(ItemComponent.CUSTOM_MODEL_DATA, value.getInt(context));
        }
    }
    
    record SetDamage(@NotNull List<LootPredicate> predicates, @NotNull LootNumber damage, boolean add) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetDamage> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetDamage::predicates,
                "damage", LootNumber.SERIALIZER, SetDamage::damage,
                "add", BinaryTagSerializer.BOOLEAN.optional(false), SetDamage::add,
                SetDamage::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            int maxDamage = input.get(ItemComponent.MAX_DAMAGE, -1);
            if (maxDamage == -1) return input;

            double damage = input.get(ItemComponent.DAMAGE, 0) / (double) maxDamage;

            double currentDura = add ? 1 - damage : 0;
            double newDura = Math.max(0, Math.min(1, currentDura + this.damage.getDouble(context)));

            double newDamage = 1 - newDura;

            return input.with(ItemComponent.DAMAGE, (int) Math.floor(newDamage * maxDamage));
        }
    }

    record SetEnchantments(@NotNull List<LootPredicate> predicates, @NotNull Map<DynamicRegistry.Key<Enchantment>, LootNumber> enchantments, boolean add) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetEnchantments> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetEnchantments::predicates,
                "enchantments", Serial.map(DynamicRegistry.Key::<Enchantment>of, DynamicRegistry.Key::name, LootNumber.SERIALIZER), SetEnchantments::enchantments,
                "add", BinaryTagSerializer.BOOLEAN.optional(false), SetEnchantments::add,
                SetEnchantments::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return EnchantmentUtils.modifyItem(input, map -> {
                this.enchantments.forEach((enchantment, number) -> {
                    int count = number.getInt(context);
                    if (add) {
                        count += map.getOrDefault(enchantment, 0);
                    }
                    map.put(enchantment, count);
                });
            });
        }
    }

    record EnchantWithLevels(@NotNull List<LootPredicate> predicates, @NotNull LootNumber levels, @Nullable List<DynamicRegistry.Key<Enchantment>> options) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<EnchantWithLevels> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), EnchantWithLevels::predicates,
                "levels", LootNumber.SERIALIZER, EnchantWithLevels::levels,
                "options", EnchantmentUtils.TAG_LIST, EnchantWithLevels::options,
                EnchantWithLevels::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return context.vanilla().enchant(context.require(LootContext.RANDOM), input, levels.getInt(context), options);
        }
    }
    
    record SetBookCover(@NotNull List<LootPredicate> predicates, @Nullable FilteredText<String> title,
                        @Nullable String author, @Nullable Integer generation) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetBookCover> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetBookCover::predicates,
                "title", FilteredText.STRING_NBT_TYPE.optional(), SetBookCover::title,
                "author", BinaryTagSerializer.STRING.optional(), SetBookCover::author,
                "generation", BinaryTagSerializer.INT.optional(), SetBookCover::generation,
                SetBookCover::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            WrittenBookContent content = input.get(ItemComponent.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);

            WrittenBookContent updated = new WrittenBookContent(
                    content.pages(),
                    title != null ? title : content.title(),
                    author != null ? author : content.author(),
                    generation != null ? generation : content.generation(),
                    content.resolved()
            );

            return input.with(ItemComponent.WRITTEN_BOOK_CONTENT, updated);
        }
    }

    record FillPlayerHead(@NotNull List<LootPredicate> predicates, @NotNull RelevantEntity entity) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<FillPlayerHead> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), FillPlayerHead::predicates,
                "entity", RelevantEntity.SERIALIZER, FillPlayerHead::entity,
                FillPlayerHead::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (!input.material().equals(Material.PLAYER_HEAD)) return input;

            if (!(context.get(entity.key()) instanceof Player player)) return input;

            PlayerSkin skin = player.getSkin();
            if (skin == null) return input;

            return input.with(ItemComponent.PROFILE, new HeadProfile(skin));
        }
    }

    record EnchantRandomly(@NotNull List<LootPredicate> predicates, @Nullable List<DynamicRegistry.Key<Enchantment>> options, boolean onlyCompatible) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<EnchantRandomly> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), EnchantRandomly::predicates,
                "options", BinaryTagSerializer.registryKey(Registries::enchantment).list().optional(), EnchantRandomly::options,
                "only_compatible", BinaryTagSerializer.BOOLEAN.optional(true), EnchantRandomly::onlyCompatible,
                EnchantRandomly::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            var reg = MinecraftServer.getEnchantmentRegistry();

            List<DynamicRegistry.Key<Enchantment>> values = new ArrayList<>();

            if (options == null) {
                reg.values().forEach(value -> values.add(reg.getKey(value)));
            } else {
                values.addAll(options);
            }

            if (onlyCompatible && !input.material().equals(Material.BOOK)) {
                values.removeIf(ench -> !reg.get(ench).supportedItems().contains(input.material()));
            }

            if (values.isEmpty()) return input;

            Random rng = context.require(LootContext.RANDOM);

            DynamicRegistry.Key<Enchantment> chosen = values.get(rng.nextInt(values.size()));

            int level = rng.nextInt(reg.get(chosen).maxLevel() + 1);

            return EnchantmentUtils.modifyItem(input, map -> map.put(chosen, level));
        }
    }

    record FurnaceSmelt(@NotNull List<LootPredicate> predicates) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<FurnaceSmelt> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), FurnaceSmelt::predicates,
                FurnaceSmelt::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            ItemStack smelted = context.vanilla().smelt(input);

            return smelted != null ? smelted.withAmount(input.amount()) : input;
        }
    }

    record ExplorationMap(@NotNull List<LootPredicate> predicates) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<ExplorationMap> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), ExplorationMap::predicates,
                ExplorationMap::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            throw new UnsupportedOperationException("TODO: Implement ExplorationMap functionality and serialization");
        }
    }

    record SetName(@NotNull List<LootPredicate> predicates, @Nullable Component name,
                   @Nullable RelevantEntity entity, @NotNull Target target) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetName> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetName::predicates,
                "name", BinaryTagSerializer.NBT_COMPONENT.optional(), SetName::name,
                "entity", RelevantEntity.SERIALIZER.optional(), SetName::entity,
                "target", Target.SERIALIZER.optional(Target.CUSTOM_NAME), SetName::target,
                SetName::new
        );
        
        public enum Target {
            ITEM_NAME("item_name", ItemComponent.ITEM_NAME),
            CUSTOM_NAME("custom_name", ItemComponent.CUSTOM_NAME);

            public static final @NotNull BinaryTagSerializer<Target> SERIALIZER = Template.constant(Target::id, Target.values());

            private final String id;
            private final DataComponent<Component> component;
            
            Target(String id, DataComponent<Component> component) {
                this.id = id;
                this.component = component;
            }

            public String id() {
                return id;
            }

            public DataComponent<Component> component() {
                return component;
            }
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (name == null) return input;

            Component component = this.name;
            // TODO: https://minecraft.wiki/w/Raw_JSON_text_format#Component_resolution
            //       This is not used in vanilla so it's fine for now.

            return input.with(target.component(), component);
        }
    }

    record SetInstrument(@NotNull List<LootPredicate> predicates) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetInstrument> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetInstrument::predicates,
                SetInstrument::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            throw new UnsupportedOperationException("TODO: Implement SetInstrument functionality and serialization");
        }
    }
    
    record SetAttributes(@NotNull List<LootPredicate> predicates, @NotNull List<AttributeDirective> modifiers, boolean replace) implements LootFunction {

        public static final @NotNull BinaryTagSerializer<SetAttributes> SERIALIZER = Template.template(
                "conditions", Serial.lazy(() -> LootPredicate.SERIALIZER).list().optional(List.of()), SetAttributes::predicates,
                "modifiers", AttributeDirective.SERIALIZER.list(), SetAttributes::modifiers,
                "replace", BinaryTagSerializer.BOOLEAN.optional(true), SetAttributes::replace,
                SetAttributes::new
        );

        public record AttributeDirective(@NotNull NamespaceID id, @NotNull Attribute attribute, @NotNull AttributeOperation operation,
                                         @NotNull LootNumber amount, @NotNull List<EquipmentSlot> slots) {

            public static final @NotNull BinaryTagSerializer<EquipmentSlot> CUSTOM_SLOT = Template.constant(
                    slot -> slot.name().toLowerCase(Locale.ROOT).replace("_", ""), EquipmentSlot.values()
            );

            public static final @NotNull BinaryTagSerializer<AttributeDirective> SERIALIZER = Template.template(
                    "id", Serial.KEY, AttributeDirective::id,
                    "attribute", Attribute.NBT_TYPE, AttributeDirective::attribute,
                    "operation", AttributeOperation.NBT_TYPE, AttributeDirective::operation,
                    "amount", LootNumber.SERIALIZER, AttributeDirective::amount,
                    "slot", Serial.coerceList(CUSTOM_SLOT), AttributeDirective::slots,
                    AttributeDirective::new
            );

        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            var component = input.get(ItemComponent.ATTRIBUTE_MODIFIERS, AttributeList.EMPTY);

            List<AttributeList.Modifier> list = replace ? new ArrayList<>() : new ArrayList<>(component.modifiers());

            for (var modifier : modifiers) {
                if (modifier.slots().isEmpty()) continue;

                AttributeModifier mod = new AttributeModifier(
                        modifier.id(),
                        modifier.amount().getDouble(context),
                        modifier.operation()
                );

                EquipmentSlot slot = modifier.slots().get(context.require(LootContext.RANDOM).nextInt(modifier.slots().size()));

                EquipmentSlotGroup group = switch (slot) {
                    case MAIN_HAND -> EquipmentSlotGroup.MAIN_HAND;
                    case OFF_HAND -> EquipmentSlotGroup.OFF_HAND;
                    case BOOTS -> EquipmentSlotGroup.FEET;
                    case LEGGINGS -> EquipmentSlotGroup.LEGS;
                    case CHESTPLATE -> EquipmentSlotGroup.CHEST;
                    case HELMET -> EquipmentSlotGroup.HEAD;
                };

                list.add(new AttributeList.Modifier(modifier.attribute(), mod, group));
            }

            return input.with(ItemComponent.ATTRIBUTE_MODIFIERS, new AttributeList(list, component.showInTooltip()));
        }
    }

}
