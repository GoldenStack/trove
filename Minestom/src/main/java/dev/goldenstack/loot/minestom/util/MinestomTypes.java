package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.converter.generator.Field;
import dev.goldenstack.loot.converter.generator.FieldTypes;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.minestom.context.LootConversionKeys;
import dev.goldenstack.loot.minestom.generation.LootPool;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import dev.goldenstack.loot.util.Utils;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for the creation of various types of Minestom-related fields.
 */
public class MinestomTypes extends FieldTypes {

    /**
     * @return a field converting number ranges
     */
    public static @NotNull Field<LootNumberRange> numberRange() {
        return Field.field(LootNumberRange.class, LootNumberRange.CONVERTER);
    }

    /**
     * @return a field converting materials
     */
    public static @NotNull Field<Material> material() {
        return implicit(String.class).map(Material.class, Material::fromNamespaceId, Material::toString);
    }

    /**
     * @return a field converting namespaced IDs
     */
    public static @NotNull Field<NamespaceID> namespaceId() {
        return implicit(String.class).map(NamespaceID.class, NamespaceID::from, NamespaceID::asString);
    }

    /**
     * @return a field converting block state checks
     */
    public static @NotNull Field<BlockStateCheck> blockStateCheck() {
        return Field.field(BlockStateCheck.class, BlockStateCheck.CONVERTER);
    }

    /**
     * @return a field converting context key groups
     */
    public static @NotNull Field<LootContextKeyGroup> keyGroup() {
        return Field.field(LootContextKeyGroup.class, Utils.createAdditive(
                (input, result, context) -> result.set(input.id()),
                (input, context) -> context.assure(LootConversionKeys.CONTEXT_KEYS).get(input.require(String.class))
        ));
    }

    /**
     * @return a field converting loot pools
     */
    public static @NotNull Field<LootPool> pool() {
        return Field.field(LootPool.class, LootPool.CONVERTER);
    }

    /**
     * @return a field converting loot tables
     */
    public static @NotNull Field<LootTable> table() {
        return Field.field(LootTable.class, LootTable.CONVERTER);
    }

}
