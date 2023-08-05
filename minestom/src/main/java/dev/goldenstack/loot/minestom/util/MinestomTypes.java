package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.minestom.generation.LootPool;
import dev.goldenstack.loot.minestom.generation.LootTable;
import dev.goldenstack.loot.minestom.modifier.*;
import dev.goldenstack.loot.minestom.nbt.ContextNBT;
import dev.goldenstack.loot.minestom.util.check.BlockStateCheck;
import dev.goldenstack.loot.minestom.util.check.EnchantmentCheck;
import dev.goldenstack.loot.minestom.util.check.ItemCheck;
import dev.goldenstack.loot.minestom.util.check.NBTCheck;
import dev.goldenstack.loot.minestom.util.nbt.NBTPath;
import dev.goldenstack.loot.serialize.generator.FieldTypes;
import dev.goldenstack.loot.serialize.generator.Serializers;
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
import java.util.function.Function;

/**
 * Utility for the creation of various types of Minestom-related fields.
 */
public class MinestomTypes {

    public static final @NotNull TypeSerializerCollection STANDARD_TYPES = TypeSerializerCollection.builder()
            .register(NamespaceID.class, FieldTypes.proxied(String.class, NamespaceID.class, NamespaceID::from, NamespaceID::asString))
            .register(Material.class, FieldTypes.proxied(String.class, Material.class, Material::fromNamespaceId, Material::name))
            .register(Block.class, FieldTypes.proxied(String.class, Block.class, Block::fromNamespaceId, Block::name))
            .register(Enchantment.class, FieldTypes.proxied(String.class, Enchantment.class, Enchantment::fromNamespaceId, Enchantment::name))
            .register(PotionType.class, FieldTypes.proxied(String.class, PotionType.class, PotionType::fromNamespaceId, PotionType::name))
            .register(PotionEffect.class, FieldTypes.proxied(String.class, PotionEffect.class, PotionEffect::fromNamespaceId, PotionEffect::name))
            .register(Attribute.class, FieldTypes.proxied(NamespaceID.class, Attribute.class, a -> Attribute.fromKey(a.asString()), a -> NamespaceID.from(a.key())))
            .register(BlockStateCheck.class, BlockStateCheck.SERIALIZER)
            .register(LootPool.class, LootPool.SERIALIZER)
            .register(LootTable.class, LootTable.SERIALIZER)
            .register(NBTPath.class, NBTPath.SERIALIZER)
            .register(LootNumberRange.class, LootNumberRange.SERIALIZER)
            .register(ContextNBT.NBTTarget.class, ContextNBT.NBTTarget.SERIALIZER)
            .register(NBTCheck.class, NBTCheck.SERIALIZER)
            .register(ItemCheck.class, ItemCheck.SERIALIZER)
            .register(EnchantmentCheck.class, EnchantmentCheck.SERIALIZER)
            .registerExact(BonusCountModifier.BonusType.class, BonusCountModifier.BonusType.TYPE_SERIALIZER)
            .register(CopyNbtModifier.Operation.class, CopyNbtModifier.Operation.SERIALIZER)
            .register(SetStewEffectModifier.StewEffect.class, SetStewEffectModifier.StewEffect.SERIALIZER)
            .register(SetAttributesModifier.AttributeDirective.class, SetAttributesModifier.AttributeDirective.SERIALIZER)
            .register(NBTCompound.class, FieldTypes.proxied(NBT.class, NBTCompound.class, input -> input instanceof NBTCompound compound ? compound : null, nbt -> nbt))
            .register(RelevantEntity.class, FieldTypes.enumerated(RelevantEntity.class, RelevantEntity::id))
            .register(AttributeSlot.class, FieldTypes.enumerated(AttributeSlot.class, operation -> operation.name().toLowerCase(Locale.ROOT)))
            .register(AttributeOperation.class, FieldTypes.enumerated(AttributeOperation.class, operation -> operation.name().toLowerCase(Locale.ROOT)))
            .register(CopyNameModifier.RelevantKey.class, FieldTypes.enumerated(CopyNameModifier.RelevantKey.class, CopyNameModifier.RelevantKey::getName))
            .register(CopyNbtModifier.Operator.class, FieldTypes.enumerated(CopyNbtModifier.Operator.class, CopyNbtModifier.Operator::id))
            .register(NBT.class, FieldTypes.join(
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


    public static @NotNull Function<Serializers.Field<Tag>, Serializers.Field<Tag>> tag(@NotNull Tag.BasicType tagType) {
        return field -> field.serializer(FieldTypes.proxied(String.class, Tag.class,
                str -> MinecraftServer.getTagManager().getTag(tagType, str),
                tag -> tag.getName().asString()
        ));
    }


}
