package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.minestom.util.nbt.NBTUtils;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

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
public record SetNbtModifier(@NotNull List<LootCondition> conditions, @NotNull NBTCompound nbt) implements ItemStackModifier {

    /**
     * A standard map-based converter for NBT set modifiers.
     */
    public static final @NotNull KeyedLootConverter<SetNbtModifier> CONVERTER =
            converter(SetNbtModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    nbtCompound().name("nbt").nodePath("tag")
            ).keyed("minecraft:set_nbt");

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var modifiedNbt = NBTUtils.merge(input.meta().toNBT(), nbt);

        // Create a builder of the same material and amount, but with the new, merged NBT.
        return ItemStack.builder(input.material())
                .amount(input.amount())
                .meta(modifiedNbt).build();
    }
}