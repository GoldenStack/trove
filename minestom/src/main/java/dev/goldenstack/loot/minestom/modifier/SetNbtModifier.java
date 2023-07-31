package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.meta.TypedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.minestom.util.nbt.NBTUtils;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.*;

/**
 * A modifier that merges each item's meta with the NBT stored in {@link #nbt()}, with the stored NBT taking precedence
 * over the item meta's NBT.
 * @param conditions the conditions required for use
 * @param nbt the nbt to set as the item's NBT
 */
public record SetNbtModifier(@NotNull List<LootCondition> conditions, @NotNull NBTCompound nbt) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:set_nbt";

    /**
     * A standard map-based converter for NBT set modifiers.
     */
    public static final @NotNull TypedLootConverter<SetNbtModifier> CONVERTER =
            converter(SetNbtModifier.class,
                    typeList(LootCondition.class).name("conditions").withDefault(List::of),
                    type(NBTCompound.class).name("nbt").nodePath("tag")
            );

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
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