package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.enchantment.EnchantmentData;
import dev.goldenstack.enchantment.EnchantmentManager;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@code LootFunction} that adds a random enchantment, with a random level, to any provided
 * {@code ItemStack}s. Enchantments are picked from {@link #enchantments()}, except if the list is empty, which will
 * make the function add a random valid enchantment.
 */
public class EnchantRandomlyFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code EnchantRandomlyFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "enchant_randomly");

    private final @NotNull EnchantmentManager manager;
    private final @NotNull ImmutableList<Enchantment> enchantments;

    /**
     * Initialize an EnchantRandomlyFunction with the provided list of possible enchantments and the EnchantmentManager.
     */
    public EnchantRandomlyFunction(@NotNull ImmutableList<LootCondition> conditions, @NotNull EnchantmentManager manager,
                                   @NotNull ImmutableList<Enchantment> enchantments){
        super(conditions);
        this.manager = manager;
        this.enchantments = enchantments;
    }

    /**
     * Returns the immutable list of enchantments that will be used to select which enchantment will be added.
     */
    public @NotNull ImmutableList<Enchantment> enchantments() {
        return enchantments;
    }

    /**
     * Returns the EnchantmentManager that is used to generate enchantments
     */
    public @NotNull EnchantmentManager enchantmentManager(){
        return manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        super.serialize(object, loader);

        JsonArray array = new JsonArray();
        for (Enchantment enchantment : this.enchantments){
            array.add(enchantment.namespace().asString());
        }

        object.add("enchantments", array);
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
     * Adds a random enchantment from {@link #enchantments()} with a random level to the item. If the list of
     * enchantments is empty, a random enchantment is selected.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        if (itemStack.getMaterial() == Material.BOOK){
            // Unsafely change the type
            //noinspection UnstableApiUsage
            itemStack = ItemStack.fromItemNBT(itemStack.toItemNBT().setString("id", Material.ENCHANTED_BOOK.namespace().asString()));
        }
        List<Enchantment> acceptableEnchantments = new ArrayList<>();
        if (this.enchantments.size() == 0) {
            if (itemStack.getMaterial() == Material.ENCHANTED_BOOK) {
                for (EnchantmentData data : this.manager.getAllEnchantmentData()) {
                    acceptableEnchantments.add(data.enchantment());
                }
            } else {
                for (EnchantmentData data : this.manager.getAllEnchantmentData()) {
                    if (data.slotType().canEnchant(itemStack)) {
                        acceptableEnchantments.add(data.enchantment());
                    }
                }
            }
        } else {
            acceptableEnchantments = this.enchantments;
        }
        Enchantment enchantment = acceptableEnchantments.get(context.findRandom().nextInt(acceptableEnchantments.size()));
        int level = context.findRandom().nextInt((int) enchantment.registry().maxLevel()) + 1;
        return itemStack.withMeta(builder -> builder.enchantment(enchantment, (short) level));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return EnchantRandomlyFunction::deserialize;
    }

    @Override
    public String toString() {
        return "EnchantRandomlyFunction[enchantments=" + enchantments + ", enchantmentManager=" + manager + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnchantRandomlyFunction that = (EnchantRandomlyFunction) o;
        return this.conditions().equals(that.conditions()) && this.enchantments.equals(that.enchantments) &&
                this.manager.equals(that.manager);
    }

    @Override
    public int hashCode() {
        return (manager.hashCode() * 31 + enchantments.hashCode()) * 31 + conditions().hashCode();
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code EnchantRandomlyFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        ImmutableList<LootCondition> list = ConditionalLootFunction.deserializeConditions(json, loader);

        JsonElement element = json.get("enchantments");
        if (element == null){
            return new EnchantRandomlyFunction(list, loader.getEnchantmentManager(), ImmutableList.of());
        }

        JsonArray array = JsonHelper.assureJsonArray(element, "enchantments");

        ImmutableList.Builder<Enchantment> builder = ImmutableList.builder();
        for (JsonElement item : array){
            NamespaceID namespaceID = JsonHelper.assureNamespaceId(item, "enchantments (while deserializing an item in the array)");
            EnchantmentData data = loader.getEnchantmentManager().getEnchantmentData(namespaceID);
            if (data == null){
                throw new JsonParseException("Invalid enchantment \"" + namespaceID + "\"! Did you initialize your enchantment manager correctly?");
            }
            builder.add(data.enchantment());
        }

        return new EnchantRandomlyFunction(list, loader.getEnchantmentManager(), builder.build());
    }
}