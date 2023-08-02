package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.converter.TypedLootConverter;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Converters.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.list;

/**
 * A modifier that decreases the size of items provided to it, following roughly normal distribution, based on the
 * explosion radius on the context.
 * @param conditions the conditions required for use
 */
public record ExplosionDecayModifier(@NotNull List<LootCondition> conditions) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:explosion_decay";

    /**
     * A standard map-based converter for explosion decay modifiers.
     */
    public static final @NotNull TypedLootConverter<ExplosionDecayModifier> CONVERTER =
            converter(ExplosionDecayModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of)
            );

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootContext context) {
        if (!LootCondition.all(conditions(), context) || !context.has(LootContextKeys.EXPLOSION_RADIUS)) {
            return input;
        }

        float radius = context.assure(LootContextKeys.EXPLOSION_RADIUS);

        int newCount = 0;
        for (int i = 0; i < input.amount(); i++) {
            if (context.random().nextDouble() <= 1 / radius) {
                newCount++;
            }
        }
        return input.withAmount(newCount);
    }

}
