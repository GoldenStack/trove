package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.FieldTypes.enumerated;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;

/**
 * Copies the name from the {@link #source()} onto the provided item.
 * @param conditions the conditions required for the copying to occur
 * @param source the source of the name
 */
public record CopyNameModifier(@NotNull List<LootCondition> conditions,
                               @NotNull RelevantKey source) implements ItemStackModifier {

    /**
     * A standard map-based converter for copy name modifiers.
     */
    public static final @NotNull KeyedLootConverter<CopyNameModifier> CONVERTER =
            converter(CopyNameModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    enumerated(RelevantKey.class, source -> source.name).name("source")
            ).keyed("minecraft:copy_name");

    private static final @NotNull Tag<Component> COMPONENT_TAG = Tag.Component("CustomName");

    /**
     * Stores the relevant loot context key. This is different from {@link dev.goldenstack.loot.minestom.util.RelevantEntity}
     * slightly, so it must be its own class.
     */
    public enum RelevantKey {
        THIS("this", LootContextKeys.THIS_ENTITY),
        KILLER("killer", LootContextKeys.KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextKeys.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextKeys.BLOCK_ENTITY);

        private final String name;
        private final LootContext.Key<?> key;

        RelevantKey(String name, LootContext.Key<?> key) {
            this.name = name;
            this.key = key;
        }
    }

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        var key = context.get(source.key);

        Component customName;
        if (key instanceof Entity entity && entity.getCustomName() != null) {
            customName = entity.getCustomName();
        } else if (key instanceof Block block && block.hasTag(COMPONENT_TAG)) {
            customName = block.getTag(COMPONENT_TAG);
        } else {
            return input;
        }

        return input.withDisplayName(customName);
    }

}
