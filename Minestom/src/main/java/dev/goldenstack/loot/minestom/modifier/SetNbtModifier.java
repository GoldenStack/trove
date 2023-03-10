package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.nbtCompound;

/**
 * A modifier that merges each item's meta with the NBT stored in {@link #nbt()}, with the stored NBT taking precedence
 * over the item meta's NBT.
 * @param conditions the conditions required for use
 * @param nbt the nbt to set as the item's NBT
 */
public record SetNbtModifier(@NotNull List<LootCondition> conditions, @NotNull NBTCompound nbt) implements LootModifier.Filtered<ItemStack> {

    /**
     * A standard map-based converter for NBT set modifiers.
     */
    public static final @NotNull KeyedLootConverter<SetNbtModifier> CONVERTER =
            converter(SetNbtModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    nbtCompound().name("nbt").nodeName("tag")
            ).keyed("minecraft:set_nbt");

    @Override
    public @Nullable Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var modifiedNbt = merge(input.meta().toNBT(), nbt);

        // Create a builder of the same material and amount, but with the new, merged NBT.
        return ItemStack.builder(input.material())
                .amount(input.amount())
                .meta(modifiedNbt).build();
    }

    @Override
    public @NotNull Type filteredType() {
        return ItemStack.class;
    }

    private static NBTCompound merge(NBTCompound base, NBTCompound changes) {
        return base.modify(mutable -> {
            for (var entry : changes) {
                var value = (mutable.get(entry.getKey()) instanceof NBTCompound baseCompound &&
                             entry.getValue() instanceof NBTCompound changeCompound) ?
                        merge(baseCompound, changeCompound) : entry.getValue();

                mutable.set(entry.getKey(), value);
            }
        });
    }
}