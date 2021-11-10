package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.enchantment.EnchantmentManager;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a {@code LootFunction} that enchants items that are provided to it.
 */
public class EnchantWithLevelsFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code EnchantWithLevelsFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "enchant_with_levels");

    private final NumberProvider level;
    private final boolean allowTreasure;
    private final EnchantmentManager enchantmentManager;

    /**
     * Initialize an EnchantWithLevelsFunction with the provided range of levels, whether or not treasure enchantments
     * are valid, and the provided EnchantmentManager.
     */
    public EnchantWithLevelsFunction(@NotNull ImmutableList<LootCondition> conditions, @NotNull NumberProvider level,
                                     boolean allowTreasure, EnchantmentManager enchantmentManager){
        super(conditions);
        this.level = level;
        this.allowTreasure = allowTreasure;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Returns the NumberProvider that determines the level that will be used
     */
    public @NotNull NumberProvider level() {
        return level;
    }

    /**
     * Returns the boolean that determines if treasure enchantments are allowed
     */
    public boolean allowTreasure() {
        return allowTreasure;
    }

    /**
     * Returns the EnchantmentManager that is used to enchant the item
     */
    public EnchantmentManager enchantmentManager() {
        return enchantmentManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        super.serialize(object, loader);
        object.add("levels", loader.getNumberProviderManager().serialize(this.level));
        object.addProperty("treasure", this.allowTreasure);
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
     * Enchants the provided ItemStack.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        itemStack = this.enchantmentManager.enchantWithLevels(
                itemStack,
                this.level.getInt(context),
                context.findRandom(),
                allowTreasure ? EnchantmentManager::discoverable : EnchantmentManager::discoverableAndNotTreasure,
                EnchantmentManager::alwaysAddIfBook
        );
        if (itemStack.getMaterial() == Material.BOOK){
            // Unsafely change the type
            //noinspection UnstableApiUsage
            itemStack = ItemStack.fromItemNBT(itemStack.toItemNBT().setString("id", Material.ENCHANTED_BOOK.namespace().asString()));
        }
        return itemStack;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return EnchantWithLevelsFunction::deserialize;
    }

    @Override
    public String toString() {
        return "EnchantWithLevelsFunction[level=" + level + ", allowTreasure=" + allowTreasure + ", enchantmentManager=" + enchantmentManager + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnchantWithLevelsFunction that = (EnchantWithLevelsFunction) o;
        return allowTreasure == that.allowTreasure && Objects.equals(level, that.level) && Objects.equals(enchantmentManager, that.enchantmentManager);
    }

    @Override
    public int hashCode() {
        return (level.hashCode() * 31 + Boolean.hashCode(allowTreasure)) * 31 + enchantmentManager.hashCode();
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code EnchantWithLevelsFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        ImmutableList<LootCondition> list = ConditionalLootFunction.deserializeConditions(json, loader);

        NumberProvider level = loader.getNumberProviderManager().deserialize(json.get("levels"), "levels");

        JsonElement treasureElement = json.get("treasure");
        boolean treasure = Boolean.TRUE.equals(JsonHelper.getAsBoolean(treasureElement));

        return new EnchantWithLevelsFunction(list, level, treasure, loader.getEnchantmentManager());

    }
}