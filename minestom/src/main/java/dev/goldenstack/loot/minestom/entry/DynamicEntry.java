package dev.goldenstack.loot.minestom.entry;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.generation.LootBatch;
import dev.goldenstack.loot.minestom.VanillaInterface;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootModifier;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Dynamically returns items based on {@link VanillaInterface#getDynamicDrops(NamespaceID, NBTCompound)} and
 * {@link #dynamicChoiceId()}.
 * @param dynamicChoiceId the namespace ID of the dynamic items to select
 * @param weight the base weight of this entry - see {@link StandardWeightedChoice#weight()}
 * @param quality the quality of this entry - see {@link StandardWeightedChoice#quality()}
 * @param modifiers the modifiers that are applied to every item provided by this entry
 * @param conditions the conditions that all must be met for any results to be generated
 */
public record DynamicEntry(@NotNull NamespaceID dynamicChoiceId, long weight, long quality,
                           @NotNull List<LootModifier> modifiers,
                           @NotNull List<LootCondition> conditions) implements StandardSingleChoice {

    public static final @NotNull String KEY = "minecraft:dynamic";

    /**
     * A standard map-based serializer for dynamic entries.
     */
    public static final @NotNull TypeSerializer<DynamicEntry> SERIALIZER =
            serializer(DynamicEntry.class,
                    field(NamespaceID.class).name("dynamicChoiceId").nodePath("name"),
                    field(long.class).name("weight").fallback(1L),
                    field(long.class).name("quality").fallback(0L),
                    field(LootModifier.class).name("modifiers").nodePath("functions").as(list()).fallback(List::of),
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
            );

    public DynamicEntry {
        modifiers = List.copyOf(modifiers);
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean shouldGenerate(@NotNull LootContext context) {
        return LootCondition.all(conditions(), context);
    }

    @Override
    public @NotNull LootBatch generate(@NotNull LootContext context) {
        var block = context.assure(LootContextKeys.BLOCK_ENTITY).block();
        var blockNBT = block.hasNbt() ? block.nbt() : new NBTCompound();

        List<ItemStack> dynamicDrops = context.assure(LootContextKeys.VANILLA_INTERFACE).getDynamicDrops(dynamicChoiceId, blockNBT);

        return LootModifier.applyAll(modifiers(), LootBatch.of(dynamicDrops), context);
    }
}
