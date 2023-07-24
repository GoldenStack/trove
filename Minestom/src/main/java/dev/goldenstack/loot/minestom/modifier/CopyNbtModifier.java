package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.nbt.LootNBT;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.minestom.util.nbt.NBTPath;
import dev.goldenstack.loot.minestom.util.nbt.NBTReference;
import dev.goldenstack.loot.minestom.util.nbt.NBTUtils;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Field.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.enumerated;
import static dev.goldenstack.loot.converter.generator.FieldTypes.loader;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;

/**
 * Copies the NBT from some {@link #source()} to the resulting item via a list of {@link #operations()}.
 * @param conditions the conditions require for copying to occur
 * @param source the source of the NBT to copy
 * @param operations the operations to apply to the NBT
 */
public record CopyNbtModifier(@NotNull List<LootCondition> conditions, @NotNull LootNBT source,
                              @NotNull List<Operation> operations) implements ItemStackModifier {

    /**
     * A standard map-based converter for copy NBT modifiers.
     */
    public static final @NotNull KeyedLootConverter<CopyNbtModifier> CONVERTER =
            converter(CopyNbtModifier.class,
                    condition().list().name("conditions").withDefault(List::of),
                    loader(LootNBT.class).name("source"),
                    field(Operation.class, Operation.CONVERTER).list().name("operations").nodePath("ops")
            ).keyed("minecraft:copy_nbt");

    /**
     * Represents an operation ({@link #operator()}) on the target NBT (selected by {@link #target()}) that is
     * determined by the source NBT (selected by {@link #source()}).
     * @param source the path of NBT to select from the source
     * @param target the path of NBT to select from the target
     * @param operator the operator to apply to the NBT
     */
    public record Operation(@NotNull NBTPath source, @NotNull NBTPath target, @NotNull Operator operator) {

        public static final @NotNull LootConverter<Operation> CONVERTER =
                converter(Operation.class,
                        field(NBTPath.class, NBTPath.CONVERTER).name("source"),
                        field(NBTPath.class, NBTPath.CONVERTER).name("target"),
                        enumerated(Operator.class, Operator::id).name("operator").nodePath("op")
                ).converter();

        /**
         * Applies this operation to the provided item, using the source if needed.
         * @param itemNBT the item to modify with this operation
         * @param sourceNBT the source NBT to use as reference
         */
        public void execute(@NotNull NBTReference itemNBT, @NotNull NBT sourceNBT) {
            List<NBT> selectedNBT = new ArrayList<>();
            for (var selected : source.get(sourceNBT)) {
                selectedNBT.add(selected.get());
            }

            if (!selectedNBT.isEmpty()) {
                this.operator.update(itemNBT, target, selectedNBT);
            }
        }
    }

    /**
     * Represents some way to combine the selected NBT onto the item NBT.
     */
    public enum Operator {

        /**
         * Sets the value of each target NBT to the last selected NBT.
         */
        REPLACE("replace") {
            @Override
            public void update(@NotNull NBTReference itemNBT, @NotNull NBTPath target, @NotNull List<NBT> selectedNBT) {
                // Last element would reasonably be the result of setting each one in order
                target.set(itemNBT, selectedNBT.get(selectedNBT.size() - 1));
            }
        },

        /**
         * Adds each selected NBT to each target NBT that is a NBT list.
         */
        APPEND("append") {
            @Override
            public void update(@NotNull NBTReference itemNBT, @NotNull NBTPath target, @NotNull List<NBT> selectedNBT) {
                List<NBTReference> nbt = target.getWithDefaults(itemNBT, () -> new NBTList<>(NBTType.TAG_End));

                for (var ref : nbt) {
                    selectedNBT.forEach(ref::tryListAdd);
                }
            }
        },

        /**
         * Merges each selected NBT onto each target NBT if both are NBT compounds.
         */
        MERGE("merge") {
            @Override
            public void update(@NotNull NBTReference itemNBT, @NotNull NBTPath target, @NotNull List<NBT> selectedNBT) {
                List<NBTReference> nbt = target.getWithDefaults(itemNBT, NBTCompound::new);

                for (var ref : nbt) {
                    if (ref.get() instanceof NBTCompound compoundRef) {
                        for (var selected : selectedNBT) {
                            if (selected instanceof NBTCompound compound) {
                                ref.set(NBTUtils.merge(compoundRef, compound));
                            }
                        }
                    }
                }
            }
        };

        private final String id;

        Operator(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public abstract void update(@NotNull NBTReference itemNBT, @NotNull NBTPath target, @NotNull List<NBT> selectedNBT);
    }

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        NBT sourceNBT = source.getNBT(context);
        NBTReference itemNBT = NBTReference.of(input.meta().toNBT());

        if (sourceNBT != null) {
            for (var operation : operations) {
                operation.execute(itemNBT, sourceNBT);
            }

            if (itemNBT.get() instanceof NBTCompound compound) {
                // Basically copy the item except with different meta
                input = ItemStack.builder(input.material()).amount(input.amount()).meta(compound).build();
            }
        }

        return input;

    }

}
