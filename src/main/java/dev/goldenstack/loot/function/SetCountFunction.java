package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code LootFunction} that can change the amount of the ItemStack that is provided.
 */
public class SetCountFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code SetCountFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "set_count");

    private final @NotNull NumberProvider count;
    private final boolean add;

    /**
     * Initialize a SetCountFunction with the provided amount and whether or not the amount will be added instead of set
     */
    public SetCountFunction(@NotNull ImmutableList<LootCondition> conditions, @NotNull NumberProvider count, boolean add){
        super(conditions);
        this.count = count;
        this.add = add;
    }

    /**
     * Returns the number provider that calculates the amount
     */
    public @NotNull NumberProvider count() {
        return count;
    }

    /**
     * Returns whether or not the value from {@link #count()} is added to the ItemStack's amount. If {@code add} is
     * {@code true}, the ItemStack's amount is incremented by the value, but if {@code add} is {@code false}, the
     * ItemStack's amount is set to the value.
     */
    public boolean add() {
        return add;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        super.serialize(object, loader);
        object.add("count", loader.getNumberProviderManager().serialize(this.count));
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
     * If {@link #add()} is false, sets the ItemStack's amount to the value of {@link #count()}. Otherwise, adds the
     * value to the ItemStack's count.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        int count = this.count.getInt(context);
        return itemStack.withAmount(add ? count + itemStack.getAmount() : count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return SetCountFunction::deserialize;
    }

    @Override
    public String toString() {
        return "SetCountFunction[conditions=" + conditions() + ", count=" + count + ", add=" + add + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetCountFunction that = (SetCountFunction) o;
        return add == that.add && count.equals(that.count);
    }

    @Override
    public int hashCode() {
        return count.hashCode() * 31 + Boolean.FALSE.hashCode();
    }

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code SetCountFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        ImmutableList<LootCondition> list = ConditionalLootFunction.deserializeConditions(json, loader);
        NumberProvider provider = loader.getNumberProviderManager().deserialize(json.get("count"), "count");
        boolean add = JsonHelper.assureBoolean(json.get("add"), "add");
        return new SetCountFunction(list, provider, add);
    }
}