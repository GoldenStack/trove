package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.converter.generator.Field;
import dev.goldenstack.loot.converter.generator.FieldTypes;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.minestom.context.LootConversionKeys;
import dev.goldenstack.loot.minestom.generation.LootPool;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import dev.goldenstack.loot.util.Utils;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.spongepowered.configurate.ConfigurateException;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

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
        return implicit(String.class).map(Material.class, Material::fromNamespaceId, Material::name);
    }

    /**
     * @return a field converting enchantments
     */
    public static @NotNull Field<Enchantment> enchantment() {
        return implicit(String.class).map(Enchantment.class, Enchantment::fromNamespaceId, Enchantment::name);
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

    /**
     * @return a field converting NBT
     */
    public static @NotNull Field<NBT> nbt() {
        return Field.field(NBT.class, Utils.createAdditive(
            (input, result, context) -> result.set(input.toSNBT()),
            (input, context) -> {
                var snbt = input.require(String.class);
                var parser = new SNBTParser(new StringReader(snbt));

                try {
                    return parser.parse();
                } catch (NBTException e) {
                    throw new ConfigurateException(input, e);
                }
            }
        ));
    }

    /**
     * Creates a field that basically treats the provided value and their identifiers as a registry, mapping them via
     * {@link NamespaceID} instances.
     * @param type the converted type
     * @param values the list of possible values of the type
     * @param identifier the function that generates the identifier for each value
     * @return a field converting whatever &lt;T&gt; is
     * @param <T> the type that will be converted
     */
    public static <T> @NotNull Field<T> identified(@NotNull Class<T> type, @NotNull Collection<T> values,
                                                   @NotNull Function<T, NamespaceID> identifier) {
        Map<NamespaceID, T> mappings = new HashMap<>();
        for (var value : values) {
            mappings.put(identifier.apply(value), value);
        }

        return Field.field(type, Utils.createAdditive(
                (input, result, context) -> result.set(type, identifier.apply(input)),
                (input, context) -> {
                    var get = mappings.get(NamespaceID.from(input.require(String.class)));
                    if (get == null) {
                        throw new ConfigurateException(input, "Expected a value of " + type + " but found something else");
                    }
                    return get;
                }
        ));
    }

    /**
     * @return a field converting attribute types
     */
    public static @NotNull Field<Attribute> attribute() {
        return identified(Attribute.class, Attribute.values(), attribute -> NamespaceID.from(attribute.key()));
    }

    /**
     * @return a field converting attribute operations
     */
    public static @NotNull Field<AttributeOperation> attributeOperation() {
        return enumerated(AttributeOperation.class, operation -> operation.name().toLowerCase(Locale.ROOT));
    }

    /**
     * @return a field converting attribute slots
     */
    public static @NotNull Field<AttributeSlot> attributeSlot() {
        return enumerated(AttributeSlot.class, operation -> operation.name().toLowerCase(Locale.ROOT));
    }

    /**
     * @return a field converting NBT compounds
     */
    public static @NotNull Field<NBTCompound> nbtCompound() {
        return nbt().map(NBTCompound.class, input -> {
            if (input instanceof NBTCompound compound) {
                return compound;
            }
            throw new ConfigurateException("Expected a NBT compound but found raw NBT: " + input);
        }, nbt -> nbt);
    }


}
