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

    public static final @NotNull TypeSerializerCollection STANDARD_TYPES = TypeSerializerCollection.builder()
            .register(NamespaceID.class, Converters.proxied(String.class, NamespaceID.class, NamespaceID::from, NamespaceID::asString))
            .register(Material.class, Converters.proxied(String.class, Material.class, Material::fromNamespaceId, Material::name))
            .register(Block.class, Converters.proxied(String.class, Block.class, Block::fromNamespaceId, Block::name))
            .register(Enchantment.class, Converters.proxied(String.class, Enchantment.class, Enchantment::fromNamespaceId, Enchantment::name))
            .register(PotionType.class, Converters.proxied(String.class, PotionType.class, PotionType::fromNamespaceId, PotionType::name))
            .register(PotionEffect.class, Converters.proxied(String.class, PotionEffect.class, PotionEffect::fromNamespaceId, PotionEffect::name))
            .register(Attribute.class, Converters.proxied(NamespaceID.class, Attribute.class, a -> Attribute.fromKey(a.asString()), a -> NamespaceID.from(a.key())))
            .register(BlockStateCheck.class, BlockStateCheck.CONVERTER)
            .register(LootPool.class, LootPool.CONVERTER)
            .register(LootTable.class, LootTable.CONVERTER)
            .register(NBTPath.class, NBTPath.CONVERTER)
            .register(LootNumberRange.class, LootNumberRange.CONVERTER)
            .register(ContextNBT.NBTTarget.class, ContextNBT.NBTTarget.CONVERTER)
            .register(NBTCheck.class, NBTCheck.CONVERTER)
            .register(ItemCheck.class, ItemCheck.CONVERTER)
            .register(EnchantmentCheck.class, EnchantmentCheck.CONVERTER)
            .register(BonusCountModifier.BonusType.class, BonusCountModifier.BonusType.TYPE_CONVERTER)
            .register(CopyNbtModifier.Operation.class, CopyNbtModifier.Operation.CONVERTER)
            .register(SetStewEffectModifier.StewEffect.class, SetStewEffectModifier.StewEffect.CONVERTER)
            .register(SetAttributesModifier.AttributeDirective.class, SetAttributesModifier.AttributeDirective.CONVERTER)
            .register(NBTCompound.class, Converters.proxied(NBT.class, NBTCompound.class, input -> input instanceof NBTCompound compound ? compound : null, nbt -> nbt))
            .register(RelevantEntity.class, FieldTypes.enumerated(RelevantEntity.class, RelevantEntity::id))
            .register(AttributeSlot.class, FieldTypes.enumerated(AttributeSlot.class, operation -> operation.name().toLowerCase(Locale.ROOT)))
            .register(AttributeOperation.class, FieldTypes.enumerated(AttributeOperation.class, operation -> operation.name().toLowerCase(Locale.ROOT)))
            .register(CopyNameModifier.RelevantKey.class, FieldTypes.enumerated(CopyNameModifier.RelevantKey.class, CopyNameModifier.RelevantKey::getName))
            .register(CopyNbtModifier.Operator.class, FieldTypes.enumerated(CopyNbtModifier.Operator.class, CopyNbtModifier.Operator::id))
            .register(NBT.class, TypedLootConverter.join(NBT.class,
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
            )
            .build();

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
