package net.goldenstack.loot;


import net.goldenstack.loot.util.ItemUtils;
import net.goldenstack.loot.util.nbt.NBTPath;
import net.goldenstack.loot.util.nbt.NBTReference;
import net.goldenstack.loot.util.nbt.NBTUtils;
import net.goldenstack.loot.util.predicate.ItemPredicate;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.ServerFlag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.*;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A function that allows loot to pass through it, potentially making modifications.
 */
public interface LootFunction {

    /**
     * Performs any mutations on the provided object and returns the result.
     * @param input the input item to this function
     * @param context the context object, to use if required
     * @return the modified form of the input
     */
    @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context);

    record All(@NotNull List<LootFunction> functions) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            for (var function : functions) {
                input = function.apply(input, context);
            }
            return input;
        }
    }

    record Filtered(@NotNull List<LootPredicate> predicates, @NotNull ItemPredicate predicate, @NotNull LootFunction function) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            return LootPredicate.all(predicates, context) && predicate.test(input) ?
                    function.apply(input, context) : input;
        }
    }

    record SetPotion(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID potion) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            PotionContents existing = input.get(ItemComponent.POTION_CONTENTS, PotionContents.EMPTY);
            PotionContents updated = new PotionContents(PotionType.fromNamespaceId(potion), existing.customColor(), existing.customEffects());

            return input.with(ItemComponent.POTION_CONTENTS, updated);
        }
    }

    record ExplosionDecay(@NotNull List<LootPredicate> predicates) implements LootFunction {
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

    record Reference(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID key) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            LootFunction function = context.require(LootContext.REGISTERED_FUNCTIONS).apply(key);

            return function != null ? function.apply(input, context) : input;
        }
    }

    record Bonus(@NotNull List<LootPredicate> predicates, @NotNull NamespaceID enchantment, @NotNull Formula formula) implements LootFunction {

        public sealed interface Formula {

            int calculate(@NotNull Random random, int count, int level);

            record Uniform(int multiplier) implements Formula {
                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    return count + random.nextInt(multiplier * level + 1);
                }
            }

            record Ore() implements Formula {
                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    if (level <= 0) return count;

                    return count * Math.min(1, random.nextInt(level + 2));
                }
            }

            record Binomial(float probability, int minTrials) implements Formula {
                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    for (int i = 0; i < minTrials + level; i++) {
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

            int level = ItemUtils.level(tool, enchantment);
            int newCount = formula.calculate(context.require(LootContext.RANDOM), input.amount(), level);

            return input.withAmount(newCount);
        }
    }

    record CopyName(@NotNull List<LootPredicate> predicates, @NotNull Target target) implements LootFunction {

        private static final @NotNull Tag<Component> BLOCK_CUSTOM_NAME = Tag.Component("CustomName");

        public enum Target {
            THIS("this", LootContext.THIS_ENTITY),
            ATTACKING_ENTITY("attacking_entity", LootContext.ATTACKING_ENTITY),
            LAST_DAMAGE_PLAYER("last_damage_player", LootContext.LAST_DAMAGE_PLAYER),
            BLOCK_ENTITY("block_entity", LootContext.BLOCK_STATE);

            private final @NotNull String id;
            private final @NotNull LootContext.Key<?> key;

            Target(@NotNull String id, @NotNull LootContext.Key<?> key) {
                this.id = id;
                this.key = key;
            }

            public @NotNull String id() {
                return id;
            }

            public @NotNull LootContext.Key<?> key() {
                return key;
            }
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Object key = context.get(target.key());

            Component customName;
            if (key instanceof Entity entity && entity.getCustomName() != null) {
                customName = entity.getCustomName();
            } else if (key instanceof Block block && block.hasTag(BLOCK_CUSTOM_NAME)) {
                customName = block.getTag(BLOCK_CUSTOM_NAME);
            } else {
                return input;
            }

            return input.with(ItemComponent.CUSTOM_NAME, customName);
        }
    }

    record ToggleTooltips(@NotNull List<LootPredicate> predicates, @NotNull Map<ComponentToggler<?>, Boolean> toggles) implements LootFunction {
        public record ComponentToggler<T>(@NotNull DataComponent<T> data, @NotNull BiFunction<T, Boolean, T> toggler) {
            public @NotNull ItemStack apply(@NotNull ItemStack input, boolean shown) {
                T component = input.get(data);
                if (component == null) return input;

                return input.with(data, toggler.apply(component, shown));
            }
        }

        public static final Map<String, ComponentToggler<?>> TOGGLERS = Stream.of(
                new ComponentToggler<>(ItemComponent.TRIM, (trim, shown) -> new ArmorTrim(trim.material(), trim.pattern(), shown)),
                new ComponentToggler<>(ItemComponent.DYED_COLOR, (color, shown) -> new DyedItemColor(color.color(), shown)),
                new ComponentToggler<>(ItemComponent.ENCHANTMENTS, (list, shown) -> new EnchantmentList(list.enchantments(), shown)),
                new ComponentToggler<>(ItemComponent.STORED_ENCHANTMENTS, (list, shown) -> new EnchantmentList(list.enchantments(), shown)),
                new ComponentToggler<>(ItemComponent.UNBREAKABLE, (unbreakable, shown) -> new Unbreakable(shown)),
                new ComponentToggler<>(ItemComponent.CAN_BREAK, (preds, shown) -> new BlockPredicates(preds.predicates(), shown)),
                new ComponentToggler<>(ItemComponent.CAN_PLACE_ON, (preds, shown) -> new BlockPredicates(preds.predicates(), shown)),
                new ComponentToggler<>(ItemComponent.ATTRIBUTE_MODIFIERS, (mods, shown) -> new AttributeList(mods.modifiers(), shown)),
                new ComponentToggler<>(ItemComponent.JUKEBOX_PLAYABLE, (playable, shown) -> new JukeboxPlayable(playable.song(), shown))
        ).collect(Collectors.toMap(toggler -> toggler.data().name(), Function.identity()));

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            for (var toggle : toggles.entrySet()) {
                input = toggle.getKey().apply(input, toggle.getValue());
            }

            return input;
        }
    }

    record StewEffect(@NotNull List<LootPredicate> predicates, @NotNull List<AddedEffect> effects) implements LootFunction {
        public record AddedEffect(@NotNull PotionEffect effect, @NotNull LootNumber duration) {}

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (!Material.SUSPICIOUS_STEW.equals(input.material()) || effects.isEmpty()) return input;

            AddedEffect effect = effects.get(context.require(LootContext.RANDOM).nextInt(effects.size()));

            long duration = effect.duration().getLong(context);
            if (!effect.effect().registry().isInstantaneous()) {
                duration *= ServerFlag.SERVER_TICKS_PER_SECOND;
            }

            SuspiciousStewEffects.Effect added = new SuspiciousStewEffects.Effect(effect.effect(), (int) duration);

            SuspiciousStewEffects current = input.get(ItemComponent.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
            return input.with(ItemComponent.SUSPICIOUS_STEW_EFFECTS, current.with(added));
        }
    }

    record OminousBottleAmplifier(@NotNull List<LootPredicate> predicates, @NotNull LootNumber amplifier) implements LootFunction {
        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            int amplifier = (int) Math.max(0, Math.min(this.amplifier.getLong(context), 4));

            return input.with(ItemComponent.OMINOUS_BOTTLE_AMPLIFIER, amplifier);
        }
    }

    record CopyNBT(@NotNull List<LootPredicate> predicates, @NotNull LootNBT source, @NotNull List<Operation> operations) implements LootFunction {
        public record Operation(@NotNull NBTPath source, @NotNull NBTPath target, @NotNull Operator operator) {
            public void execute(@NotNull NBTReference nbt, @NotNull BinaryTag sourceTag) {
                List<BinaryTag> nbts = new ArrayList<>();
                source.get(sourceTag).forEach(ref -> nbts.add(ref.get()));

                if (nbts.isEmpty()) return;
                operator.merge(nbt, target, nbts);
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

            public abstract void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source);
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            BinaryTag sourceNBT = source.getNBT(context);
            if (sourceNBT == null) return input;

            NBTReference targetNBT = NBTReference.of(input.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).nbt());

            for (Operation operation : operations) {
                operation.execute(targetNBT, sourceNBT);
            }

            if (targetNBT.get() instanceof CompoundBinaryTag compound) {
                return input.with(ItemComponent.CUSTOM_DATA, new CustomData(compound));
            } else {
                return input;
            }
        }
    }


}
