package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Field.field;
import static dev.goldenstack.loot.converter.generator.FieldTypes.number;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.condition;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.namespaceId;

/**
 * Adds a random potion effect, from the pool, to each provided suspicious stew item.
 * @param conditions the conditions required to add the effects
 * @param effects the effect pool, from which one will be picked and applied
 */
@SuppressWarnings("UnstableApiUsage")
public record SetStewEffectModifier(@NotNull List<LootCondition> conditions,
                                    @NotNull List<StewEffect> effects) implements ItemStackModifier {

    /**
     * A standard map-based converter for set stew effect modifiers.
     */
    public static final @NotNull KeyedLootConverter<SetStewEffectModifier> CONVERTER =
            converter(SetStewEffectModifier.class,
                    condition().list().name("conditions").withDefault(List::of),
                    field(StewEffect.class, StewEffect.CONVERTER).list().name("effects")
            ).keyed("minecraft:set_stew_effect");

    /**
     * Holds an effect and its unresolved duration.
     * @param effect the effect to apply
     * @param duration the number that will provide the duration (measured in seconds)
     */
    public record StewEffect(@NotNull PotionEffect effect, @NotNull LootNumber duration) {
        public static final @NotNull AdditiveConverter<StewEffect> CONVERTER =
                converter(StewEffect.class,
                        namespaceId().map(PotionEffect.class, PotionEffect::fromNamespaceId, PotionEffect::namespace).name("effect").nodePath("type"),
                        number().name("duration")
                ).additive();
    }

    private static final @NotNull Tag<List<NBT>> EFFECTS_NBT = Tag.NBT("Effects").list().defaultValue(List::of);

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        if (input.material() != Material.SUSPICIOUS_STEW || effects.isEmpty()) {
            return input;
        }

        var effect = effects().get(context.random().nextInt(effects().size()));

        var duration = (int) effect.duration().getLong(context);

        // Adjust for the duration unit being seconds
        if (!effect.effect().registry().isInstantaneous()) {
            duration *= 20;
        }

        // A custom tag serializer may be more convenient in some cases,
        // but this is the only instance of serialization or deserialization of this type
        MutableNBTCompound compound = new MutableNBTCompound();
        compound.setInt("EffectId", effect.effect().id());
        compound.setInt("EffectDuration", duration);

        return input.withMeta(builder -> {
            var effects = new ArrayList<>(builder.getTag(EFFECTS_NBT));

            effects.add(compound.toCompound());

            builder.setTag(EFFECTS_NBT, effects);
        });
    }

}
