package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.converter.LootConverter;
import dev.goldenstack.loot.converter.generator.Field;
import dev.goldenstack.loot.converter.generator.FieldTypes;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeyGroup;
import dev.goldenstack.loot.minestom.generation.LootPool;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.nbt.LootNBT;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.spongepowered.configurate.ConfigurateException;

import java.io.StringReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.goldenstack.loot.converter.generator.Field.field;

/**
 * Utility for the creation of various types of Minestom-related fields.
 */
public class MinestomTypes extends FieldTypes {

    public static @NotNull Field<LootNBT> lootNBT() {
        return field(LootNBT.class, loader(LootNBT.class));
    }

    /**
     * @return a field converting number ranges
     */
    public static @NotNull Field<LootNumberRange> numberRange() {
        return field(LootNumberRange.class, LootNumberRange.CONVERTER);
    }

    /**
     * @return a field converting materials
     */
    public static @NotNull Field<Material> material() {
        return implicit(String.class).map(Material.class, Material::fromNamespaceId, Material::name);
    }

    /**
     * @return a field converting blocks
     */
    public static @NotNull Field<Block> block() {
        return implicit(String.class).map(Block.class, Block::fromNamespaceId, Block::name);
    }

    /**
     * @return a field converting enchantments
     */
    public static @NotNull Field<Enchantment> enchantment() {
        return implicit(String.class).map(Enchantment.class, Enchantment::fromNamespaceId, Enchantment::name);
    }

    /**
     * @return a field converting potion types
     */
    public static @NotNull Field<PotionType> potionType() {
        return implicit(String.class).map(PotionType.class, PotionType::fromNamespaceId, PotionType::name);
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
        return field(BlockStateCheck.class, BlockStateCheck.CONVERTER);
    }

    /**
     * @return a field converting location predicates
     */
    public static @NotNull Field<VanillaInterface.LocationPredicate> locationPredicate() {
        return field(VanillaInterface.LocationPredicate.class, loader(VanillaInterface.LocationPredicate.class));
    }

    /**
     * @return a field converting entity predicates
     */
    public static @NotNull Field<VanillaInterface.EntityPredicate> entityPredicate() {
        return field(VanillaInterface.EntityPredicate.class, loader(VanillaInterface.EntityPredicate.class));
    }

    /**
     * @return a field converting relevant entities
     */
    public static @NotNull Field<RelevantEntity> relevantEntity() {
        return enumerated(RelevantEntity.class, RelevantEntity::id);
    }

    /**
     * @return a field converting context key groups
     */
    public static @NotNull Field<LootContextKeyGroup> keyGroup() {
        return field(LootContextKeyGroup.class, loader(LootContextKeyGroup.class));
    }

    /**
     * @return a field converting the provided basic tag type
     */
    public static @NotNull Field<Tag> tag(@NotNull Tag.BasicType tagType) {
        return implicit(String.class).map(Tag.class,
                str -> MinecraftServer.getTagManager().getTag(tagType, str),
                tag -> tag.getName().asString()
        );
    }

    /**
     * @return a field converting loot pools
     */
    public static @NotNull Field<LootPool> pool() {
        return field(LootPool.class, LootPool.CONVERTER);
    }

    /**
     * @return a field converting loot tables
     */
    public static @NotNull Field<LootTable> table() {
        return field(LootTable.class, LootTable.CONVERTER);
    }

    /**
     * @return a field converting NBT
     */
    public static @NotNull Field<NBT> nbt() {
        return field(NBT.class, LootConverter.join(
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
        Map<NamespaceID, T> mappings = values.stream().collect(Collectors.toMap(identifier, Function.identity()));
        return namespaceId().map(type, mappings::get, identifier::apply);
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
        return nbt().map(NBTCompound.class, input -> input instanceof NBTCompound compound ? compound : null, nbt -> nbt);
    }


}
