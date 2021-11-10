package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.enchantment.EnchantmentData;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Represents a {@code LootFunction} that adds enchantments to the ItemStack that is provided.
 */
public class SetEnchantmentsFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code SetEnchantmentsFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "set_enchantments");

    private final @NotNull ImmutableMap<Enchantment, NumberProvider> enchantments;
    private final boolean add;

    /**
     * Initialize a SetEnchantmentsFunction with the provided enchantments and whether or not the enchantments already
     * on the item will get removed.
     */
    public SetEnchantmentsFunction(@NotNull ImmutableList<LootCondition> conditions,
                                   @NotNull ImmutableMap<Enchantment, NumberProvider> enchantments, boolean add){
        super(conditions);
        this.enchantments = enchantments;
        this.add = add;
    }

    /**
     * Returns the immutable map of enchantments that will be given to the item
     */
    public @NotNull ImmutableMap<Enchantment, NumberProvider> enchantments() {
        return enchantments;
    }

    /**
     * Returns whether or not the enchantments already on the item will be removed when this function is applied to it.
     * If this is true, the enchantments will not be removed because they are just getting added, but if this is false,
     * the enchantments that are already on the item will not be kept.
     */
    public boolean add() {
        return add;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        super.serialize(object, loader);

        JsonObject enchantments = new JsonObject();
        for (var entry : this.enchantments.entrySet()){
            enchantments.add(entry.getKey().name(), loader.getNumberProviderManager().serialize(entry.getValue()));
        }

        object.add("enchantments", enchantments);
        object.addProperty("add", this.add);
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
     * If {@link #add()} is false, sets the ItemStack's enchantments to {@link #enchantments()}. Otherwise, just adds
     * them, preserving any current enchantments. <b>If you have a custom ItemMeta implementation that has a different
     * way to add enchantments to an item, you will have to extend this class.</b>
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        return itemStack.withMeta(im -> {
            if (!add){
                im.enchantments(new HashMap<>());
            }
            for (var entry : this.enchantments.entrySet()){
                im.enchantment(entry.getKey(), (short) entry.getValue().getInt(context));
            }
            return im;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return SetEnchantmentsFunction::deserialize;
    }

    @Override
    public String toString() {
        return "SetEnchantmentsFunction[enchantments=" + enchantments + ", add=" + add + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetEnchantmentsFunction that = (SetEnchantmentsFunction) o;
        return add == that.add && enchantments.equals(that.enchantments);
    }

    @Override
    public int hashCode() {
        return enchantments.hashCode() * 31 + Boolean.hashCode(add);
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code SetEnchantmentsFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        ImmutableList<LootCondition> list = ConditionalLootFunction.deserializeConditions(json, loader);

        JsonObject object = JsonHelper.assureJsonObject(json.get("enchantments"), "enchantments");
        ImmutableMap.Builder<Enchantment, NumberProvider> builder = ImmutableMap.builder();

        for (var entry : object.entrySet()){
            NamespaceID namespaceID = NamespaceID.from(entry.getKey());

            EnchantmentData data = loader.getEnchantmentManager().getEnchantmentData(namespaceID);

            if (data == null){
                throw new JsonParseException("Invalid enchantment \"" + namespaceID + "\"! Did you initialize your enchantment manager correctly?");
            }

            builder.put(data.enchantment(), loader.getNumberProviderManager().deserialize(entry.getValue(), entry.getKey()));
        }

        boolean add = JsonHelper.assureBoolean(json.get("add"), "add");

        return new SetEnchantmentsFunction(list, builder.build(), add);
    }
}