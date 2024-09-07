package net.goldenstack.loot;

import net.goldenstack.loot.util.Template;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot table.
 * @param pools the pools that generate items in this table
 * @param functions the functions applied to each output item of this table
 * @param randomSequence An ID specifying the name of the random sequence that is used to generate loot from this loot table.
 */
public record LootTable(@NotNull List<LootPool> pools, @NotNull List<LootFunction> functions, @Nullable NamespaceID randomSequence) implements LootGenerator {

    public static final @NotNull LootTable EMPTY = new LootTable(List.of(), List.of(), null);

    @SuppressWarnings("UnstableApiUsage")
    public static final @NotNull BinaryTagSerializer<LootTable> SERIALIZER = Template.template(
            "pools", LootPool.SERIALIZER.list(), LootTable::pools,
            "functions", LootFunction.SERIALIZER.list(), LootTable::functions,
            "random_sequence", Template.template(() -> null), LootTable::randomSequence,
            LootTable::new
    );

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
        List<ItemStack> items = new ArrayList<>();

        for (var pool : pools) {
            for (var item : pool.generate(context)) {
                items.add(LootFunction.apply(functions, item, context));
            }
        }

        return items;
    }
}
