package net.goldenstack.loot.util.predicate;

import net.goldenstack.loot.LootContext;
import net.goldenstack.loot.util.LootNumberRange;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// TODO: Incomplete

@SuppressWarnings("UnstableApiUsage")
public record ItemPredicate(@Nullable RegistryTag<Material> items, @NotNull LootNumberRange count, @Nullable DataComponentMap components) {

    public static final @NotNull StructCodec<ItemPredicate> CODEC = StructCodec.struct(
            "items", RegistryTag.codec(Registries::material), ItemPredicate::items,
            "count", LootNumberRange.CODEC.optional(new LootNumberRange(null, null)), ItemPredicate::count,
            "components", DataComponent.MAP_NBT_TYPE.optional(), ItemPredicate::components,
            ItemPredicate::new
    );

    public boolean test(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        if (items != null && !items.contains(itemStack.material())) return false;

        if (!count.check(context, itemStack.amount())) return false;

        if (components != null) {
            for (DataComponent.Value entry : components.entrySet()) {
                if (!Objects.equals(itemStack.get(entry.component()), entry.value())) return false;
            }
        }

        // TODO: Incomplete
        //       Implement predicates

        return false;
    }

}
