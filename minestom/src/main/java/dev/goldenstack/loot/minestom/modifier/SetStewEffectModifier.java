package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootContext;
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
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

import static dev.goldenstack.loot.serialize.generator.FieldTypes.list;
import static dev.goldenstack.loot.serialize.generator.Serializers.field;
import static dev.goldenstack.loot.serialize.generator.Serializers.serializer;

/**
 * Adds a random potion effect, from the pool, to each provided suspicious stew item.
 * @param conditions the conditions required to add the effects
 * @param effects the effect pool, from which one will be picked and applied
 */
@SuppressWarnings("UnstableApiUsage")
public record SetStewEffectModifier(@NotNull List<LootCondition> conditions,
                                    @NotNull List<StewEffect> effects) implements ItemStackModifier {

    public static final @NotNull String KEY = "minecraft:set_stew_effect";

    /**
     * A standard map-based serializer for set stew effect modifiers.
     */
    public static final @NotNull TypeSerializer<SetStewEffectModifier> SERIALIZER =
            serializer(SetStewEffectModifier.class,
                    field(LootCondition.class).name("conditions").as(list()).fallback(List::of),
                    field(StewEffect.class).name("effects").as(list())
            );

    /**
     * Holds an effect and its unresolved duration.
     * @param effect the effect to apply
     * @param duration the number that will provide the duration (measured in seconds)
     */
    public record StewEffect(@NotNull PotionEffect effect, @NotNull LootNumber duration) {
        public static final @NotNull TypeSerializer<StewEffect> SERIALIZER =
                serializer(StewEffect.class,
                        field(PotionEffect.class).name("effect").nodePath("type"),
                        field(LootNumber.class).name("duration")
                );
    }

    private static final @NotNull Tag<List<NBT>> EFFECTS_NBT = Tag.NBT("Effects").list().defaultValue(List::of);

    @Override
    public @NotNull Object modifyTyped(@NotNull ItemStack input, @NotNull LootContext context) {
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
