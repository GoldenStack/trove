package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import dev.goldenstack.loot.util.EnumValues;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a {@code LootFunction} that adds attributes to the provided ItemStack.<br>
 * Contrary to the path of {@link #KEY}, this preserves attributes that were already on the ItemStack.
 */
public class AddAttributesFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code AddAttributesFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "set_attributes");

    private final ImmutableList<Modifier> modifiers;

    /**
     * Initializes an AddAttributesFunction with the provided modifiers
     */
    public AddAttributesFunction(@NotNull ImmutableList<LootCondition> conditions, @NotNull ImmutableList<Modifier> modifiers){
        super(conditions);
        this.modifiers = modifiers;
    }

    /**
     * Returns the modifiers that get added to items that are provided to this instance.
     */
    public @NotNull ImmutableList<Modifier> modifiers() {
        return modifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        super.serialize(object, loader);
        JsonArray array = new JsonArray();
        for (Modifier modifier : this.modifiers){
            array.add(modifier.serialize(loader));
        }
        object.add("modifiers", array);
    }

    /**
     * {@inheritDoc}
     * @return {@link #KEY}
     */
    @Override
    public @NotNull NamespaceID getKey() {
        return KEY;
    }

    /**
     * Adds the modifiers from {@link #modifiers()} to the provided ItemStack.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        return itemStack.withMeta(builder -> {
            List<ItemAttribute> attributeList = new ArrayList<>();
            for (Modifier modifier : this.modifiers){
                attributeList.add(modifier.toItemAttribute(context));
            }
            return builder.attributes(attributeList);
        });
    }

    /**
     * Represents information about an Attribute. This is used instead of an ItemAttribute because it can randomly pick
     * from a list of {@code AttributeSlot}s, create random UUIDs if it doesn't have one, and generate attribute values
     * dynamically, through a NumberProvider.
     */
    public static class Modifier {
        private final @NotNull Attribute attribute;
        private final @NotNull ImmutableList<AttributeSlot> slots;
        private final @NotNull AttributeOperation operation;
        private final @NotNull NumberProvider amount;

        private final @Nullable UUID uuid;
        private final @Nullable String name;

        /**
         * Creates a new Modifier
         * @param attribute The attribute to use
         * @param slots The list of slots that can be picked
         * @param operation The operation to use
         * @param amount The NumberProvider that will be used to generate values
         * @param uuid The UUID to use, or null if it should be randomly generated
         * @param name The name to use. If this is null, Modifier#toItemAttribute will use an empty string instead.
         */
        public Modifier(@NotNull Attribute attribute, @NotNull ImmutableList<AttributeSlot> slots, @NotNull AttributeOperation operation,
                        @NotNull NumberProvider amount, @Nullable UUID uuid, @Nullable String name){
            this.attribute = attribute;
            if (slots.size() == 0){
                throw new InvalidParameterException("Parameter \"slots\" must have at least one element!");
            }
            this.slots = slots;
            this.operation = operation;
            this.amount = amount;
            this.uuid = uuid;
            this.name = name;
        }

        /**
         * Returns the attribute that is used when creating ItemAttribute instances.
         */
        public @NotNull Attribute attribute(){
            return this.attribute;
        }

        /**
         * Returns the list of attribute slots. When creating an ItemAttribute instance, a random slot from the list is
         * picked. This is guaranteed to have at least one element, because an exception is thrown during initialization
         * if that is the case.
         */
        public @NotNull ImmutableList<AttributeSlot> slots() {
            return slots;
        }

        /**
         * Returns the operation that is used when ItemAttribute instances are created.
         */
        public @NotNull AttributeOperation operation() {
            return operation;
        }

        /**
         * Returns the NumberProvider that is used to calculate the value for the ItemAttribute that gets created.
         */
        public @NotNull NumberProvider amount() {
            return amount;
        }

        /**
         * Returns the UUID that is used when {@link #toItemAttribute(LootContext)} is run. If this is null, a random
         * UUID is used whenever the method is called.
         */
        public @Nullable UUID uuid() {
            return uuid;
        }

        /**
         * Returns the name that is used when {@link #toItemAttribute(LootContext)} is run. If this is null, an empty
         * string ("") is used.
         */
        public @Nullable String name() {
            return name;
        }

        /**
         * Turns this Modifier into an ItemAttribute. If this instance's UUID is null, a random UUID will be used - but
         * this instance's UUID will stay null, so it gets randomized again next time. If this instance's name is null,
         * an empty string is used.
         * @param context The context to use (the only use of this in the method is to get the value from {@link #amount()}).
         * @return The ItemAttribute that was generated
         */
        public @NotNull ItemAttribute toItemAttribute(@NotNull LootContext context){
            return new ItemAttribute(
                    this.uuid == null ? UUID.randomUUID() : this.uuid,
                    this.name == null ? "" : this.name,
                    this.attribute,
                    this.operation,
                    this.amount.getDouble(context),
                    this.slots.get(ThreadLocalRandom.current().nextInt(this.slots.size()))
            );
        }

        /**
         * Serializes this Modifier to a JsonObject.
         */
        public @NotNull JsonObject serialize(@NotNull ImmuTables loader) throws JsonParseException {
            JsonObject object = new JsonObject();
            object.addProperty("attribute", this.attribute.getKey());
            object.addProperty("operation", this.operation.name().toLowerCase(Locale.ROOT));
            object.add("amount", loader.getNumberProviderManager().serialize(this.amount));
            if (this.name != null){
                object.addProperty("name", this.name);
            }
            if (this.uuid != null){
                object.addProperty("id", this.uuid.toString());
            }
            if (this.slots.size() == 1){
                object.addProperty("slot", this.slots.get(0).toString().toLowerCase(Locale.ROOT));
            } else {
                JsonArray array = new JsonArray();
                for (AttributeSlot slot : this.slots){
                    array.add(slot.toString().toLowerCase(Locale.ROOT));
                }
                object.add("slot", array);
            }
            return object;
        }

        /**
         * Deserializes the provided JsonObject into a Modifier.
         */
        public static @NotNull Modifier deserialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {

            Attribute attribute = Attribute.fromKey(JsonHelper.assureString(object.get("attribute"), "attribute"));
            if (attribute == null) {
                throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid attribute", "attribute", null));
            }

            AttributeOperation operation = EnumValues.ATTRIBUTE_OPERATION.valueOf(JsonHelper.assureString(object.get("operation"), "operation").toUpperCase(Locale.ROOT));
            if (operation == null) {
                throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid attribute operation", "operation", null));
            }

            final @NotNull NumberProvider amount = loader.getNumberProviderManager().deserialize(object.get("amount"), "amount");

            JsonElement nameElement = object.get("name");
            final @Nullable String name = (JsonHelper.isNull(nameElement) ? null : JsonHelper.assureString(nameElement, "name"));

            JsonElement uuidElement = object.get("id");
            final @Nullable UUID uuid = (JsonHelper.isNull(uuidElement) ? null : JsonHelper.assureUUID(uuidElement, "id"));

            JsonElement slotElement = JsonHelper.assureNotNull(object.get("slot"), "slot");
            ImmutableList<AttributeSlot> slots;

            String string = JsonHelper.getAsString(slotElement);
            if (string == null){
                JsonArray array = JsonHelper.getAsJsonArray(slotElement);
                if (array == null){
                    throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid attribute slot or an array of attribute slots", "slot", slotElement));
                }
                AttributeSlot[] slotArray = new AttributeSlot[array.size()];
                for (int i = 0; i < array.size(); i++){
                    JsonElement element = array.get(i);
                    String slotString = JsonHelper.getAsString(element);
                    if (slotString == null){
                        throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid attribute slot (while deserializing an element in an array)", "slot", element));
                    }
                    AttributeSlot slot = EnumValues.ATTRIBUTE_SLOT.valueOf(slotString.toUpperCase(Locale.ROOT));
                    if (slot == null){
                        throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid attribute slot (while deserializing an element in an array)", "slot", element));
                    }
                    slotArray[i] = slot;
                }
                slots = ImmutableList.copyOf(slotArray);
            } else {
                AttributeSlot slot = EnumValues.ATTRIBUTE_SLOT.valueOf(string.toUpperCase(Locale.ROOT));
                if (slot == null) {
                    throw new JsonParseException(JsonHelper.createExpectedValueMessage("a valid attribute slot", "slot", null));
                }
                slots = ImmutableList.of(slot);
            }

            return new Modifier(attribute, slots, operation, amount, uuid, name);
        }

        @Override
        public String toString() {
            return "AddAttributesFunction.Modifier[attribute=" + attribute + ", slots=" + slots +
                    ", operation=" + operation + ", amount=" + amount + ", uuid=" + uuid + ", name=" + name + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Modifier modifier = (Modifier) o;
            return attribute.equals(modifier.attribute) && slots.equals(modifier.slots) &&
                    operation == modifier.operation && amount.equals(modifier.amount) &&
                    Objects.equals(uuid, modifier.uuid) && Objects.equals(name, modifier.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attribute, slots, operation, amount, uuid, name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return AddAttributesFunction::deserialize;
    }

    @Override
    public String toString() {
        return "AddAttributesFunction[conditions=" + conditions() + ", modifiers=" + modifiers + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddAttributesFunction that = (AddAttributesFunction) o;
        return conditions().equals(that.conditions()) && Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode() {
        return modifiers.hashCode() * 31 + conditions().hashCode();
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code AddAttributesFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        ImmutableList<LootCondition> conditions = ConditionalLootFunction.deserializeConditions(json, loader);

        JsonArray array = JsonHelper.assureJsonArray(json.get("modifiers"), "modifiers");
        Modifier[] modifiers = new Modifier[array.size()];
        for (int i = 0; i < array.size(); i++){
            modifiers[i] = Modifier.deserialize(JsonHelper.assureJsonObject(array.get(i), "modifiers (while deserializing an element)"), loader);
        }

        return new AddAttributesFunction(conditions, ImmutableList.copyOf(modifiers));
    }
}