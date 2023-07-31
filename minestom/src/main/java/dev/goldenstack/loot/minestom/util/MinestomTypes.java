package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.converter.generator.Converters;
import dev.goldenstack.loot.converter.generator.Converters.Field;
import dev.goldenstack.loot.converter.generator.FieldTypes;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.generation.LootPool;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.modifier.*;
import dev.goldenstack.loot.minestom.nbt.ContextNBT;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import dev.goldenstack.loot.minestom.util.check.EnchantmentCheck;
import dev.goldenstack.loot.minestom.util.check.ItemCheck;
import dev.goldenstack.loot.minestom.util.check.NBTCheck;
import dev.goldenstack.loot.minestom.util.nbt.NBTPath;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.StringReader;
import java.util.Locale;

/**
 * Utility for the creation of various types of Minestom-related fields.
 */
public class MinestomTypes {

    public static final @NotNull TypeSerializerCollection STANDARD_TYPES = FieldTypes.wrap(
            Converters.proxied(String.class, NamespaceID.class, NamespaceID::from, NamespaceID::asString),
            Converters.proxied(String.class, Material.class, Material::fromNamespaceId, Material::name),
            Converters.proxied(String.class, Block.class, Block::fromNamespaceId, Block::name),
            Converters.proxied(String.class, Enchantment.class, Enchantment::fromNamespaceId, Enchantment::name),
            Converters.proxied(String.class, PotionType.class, PotionType::fromNamespaceId, PotionType::name),
            Converters.proxied(String.class, PotionEffect.class, PotionEffect::fromNamespaceId, PotionEffect::name),
            Converters.proxied(NamespaceID.class, Attribute.class, a -> Attribute.fromKey(a.asString()), a -> NamespaceID.from(a.key())),
            BlockStateCheck.CONVERTER,
            LootPool.CONVERTER,
            LootTable.CONVERTER,
            NBTPath.CONVERTER,
            LootNumberRange.CONVERTER,
            ContextNBT.TARGET_CONVERTER,
            NBTCheck.CONVERTER,
            ItemCheck.CONVERTER,
            EnchantmentCheck.CONVERTER,
            BonusCountModifier.BonusType.TYPE_CONVERTER,
            CopyNbtModifier.Operation.CONVERTER,
            SetStewEffectModifier.StewEffect.CONVERTER,
            SetAttributesModifier.AttributeDirective.CONVERTER,
            Converters.proxied(NBT.class, NBTCompound.class, input -> input instanceof NBTCompound compound ? compound : null, nbt -> nbt),
            FieldTypes.enumerated(RelevantEntity.class, RelevantEntity::id),
            FieldTypes.enumerated(AttributeSlot.class, operation -> operation.name().toLowerCase(Locale.ROOT)),
            FieldTypes.enumerated(AttributeOperation.class, operation -> operation.name().toLowerCase(Locale.ROOT)),
            FieldTypes.enumerated(CopyNameModifier.RelevantKey.class, CopyNameModifier.RelevantKey::getName),
            FieldTypes.enumerated(CopyNbtModifier.Operator.class, CopyNbtModifier.Operator::id),
            TypedLootConverter.join(NBT.class,
                    (input, result) -> result.set(input.toSNBT()),
                    input -> {
                        var snbt = input.require(String.class);
                        var parser = new SNBTParser(new StringReader(snbt));

                        try {
                            return parser.parse();
                        } catch (NBTException e) {
                            throw new SerializationException(input, NBT.class, e);
                        }
                    }
            )
    );

    /**
     * @return a field converting the provided basic tag type
     */
    public static @NotNull Field<Tag> tag(@NotNull Tag.BasicType tagType) {
        return Converters.field(Converters.proxied(String.class, Tag.class,
                str -> MinecraftServer.getTagManager().getTag(tagType, str),
                tag -> tag.getName().asString()
        ));
    }


}
