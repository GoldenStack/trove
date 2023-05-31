package dev.goldenstack.loot.minestom.modifier;

import dev.goldenstack.loot.context.LootGenerationContext;
import dev.goldenstack.loot.converter.additive.AdditiveConverter;
import dev.goldenstack.loot.converter.meta.KeyedLootConverter;
import dev.goldenstack.loot.minestom.util.ItemStackModifier;
import dev.goldenstack.loot.minestom.util.MinestomTypes;
import dev.goldenstack.loot.structure.LootCondition;
import dev.goldenstack.loot.structure.LootNumber;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static dev.goldenstack.loot.converter.generator.Converters.converter;
import static dev.goldenstack.loot.converter.generator.Field.field;
import static dev.goldenstack.loot.minestom.util.MinestomTypes.*;

/**
 * A modifier that adds a list of attributes to each provided item.
 * @param conditions the conditions required for use
 * @param attributes the attributes to apply to each item
 */
public record SetAttributesModifier(@NotNull List<LootCondition> conditions,
                                    @NotNull List<AttributeDirective> attributes) implements ItemStackModifier {

    /**
     * A standard map-based converter for set attribute modifiers.
     */
    public static final @NotNull KeyedLootConverter<SetAttributesModifier> CONVERTER =
            converter(SetAttributesModifier.class,
                    condition().list().name("conditions").withDefault(ArrayList::new),
                    field(AttributeDirective.class, AttributeDirective.CONVERTER).list().name("attributes").nodeName("modifiers")
            ).keyed("minecraft:set_attributes");

    @Override
    public @NotNull Object modify(@NotNull ItemStack input, @NotNull LootGenerationContext context) {
        if (!LootCondition.all(conditions(), context)) {
            return input;
        }

        List<ItemAttribute> appliedAttributes = new ArrayList<>(input.meta().getAttributes());
        Random random = context.random();
        for (var attribute : attributes) {
            UUID uuid = attribute.id() != null ? attribute.id() : UUID.randomUUID();
            AttributeSlot slot = attribute.slots().get(random.nextInt(attribute.slots().size()));

            var generated = new ItemAttribute(uuid, attribute.name(), attribute.attribute(), attribute.operation(), attribute.amount().getDouble(context), slot);
            appliedAttributes.add(generated);
        }

        return input.withMeta(meta -> meta.attributes(appliedAttributes));
    }

    /**
     * A directive for how an attribute should be added.
     * @param name the name of the added item attribute
     * @param attribute the attribute type to add
     * @param operation the operation of the attribute
     * @param amount the amount to apply to the attribute value, considering the operation
     * @param id the (optional) id of the attribute; if set as null a random one will be applied each time instead
     * @param slots the list of slots to randomly choose from when adding the attribute
     */
    public record AttributeDirective(@NotNull String name, @NotNull Attribute attribute,
                                     @NotNull AttributeOperation operation, @NotNull LootNumber amount,
                                     @Nullable UUID id, @NotNull List<AttributeSlot> slots) {

        public static final @NotNull AdditiveConverter<AttributeDirective> CONVERTER =
                converter(AttributeDirective.class,
                        implicit(String.class).name("name"),
                        MinestomTypes.attribute().name("attribute"),
                        attributeOperation().name("operation"),
                        number().name("amount"),
                        uuid().name("id").optional(),
                        attributeSlot().possibleList().name("slots").nodeName("slot")
                ).additive();

    }
}
