package dev.goldenstack.loot.minestom.util;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.ProtocolObject;
import org.jetbrains.annotations.NotNull;

public class Converters {
    private Converters() {}

    public static final @NotNull RegistryConverter<ItemStack, Block> BLOCK_CONVERTER = new RegistryConverter<>(ProtocolObject::name, Block::fromNamespaceId);

    public static final @NotNull RegistryConverter<ItemStack, Material> MATERIAL_CONVERTER = new RegistryConverter<>(ProtocolObject::name, Material::fromNamespaceId);

    public static final @NotNull RegistryConverter<ItemStack, PotionType> POTION_TYPE_CONVERTER = new RegistryConverter<>(ProtocolObject::name, PotionType::fromNamespaceId);

    public static final @NotNull RegistryConverter<ItemStack, Tag> ITEM_TAG_CONVERTER = new RegistryConverter<>(
            tag -> tag.getName().asString(),
            string -> MinecraftServer.getTagManager().getTag(Tag.BasicType.ITEMS, string)
    );

}
