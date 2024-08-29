package net.goldenstack.loot.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExternalTags {

    private ExternalTags() {}

    public static final @NotNull Tag<Component> CUSTOM_NAME = Tag.Component("CustomName");

    public static final @NotNull Tag<List<Material>> DECORATED_POT_SHERDS = Tag.String("sherds")
            .map(NamespaceID::from, NamespaceID::asString)
            .map(Material::fromNamespaceId, Material::namespace)
            .list().defaultValue(List::of);

    public static final @NotNull Tag<List<ItemStack>> CONTAINER_ITEMS = Tag.ItemStack("Items").list().defaultValue(List::of);

}
