package dev.goldenstack.loot.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonHelper;
import dev.goldenstack.loot.json.JsonLootConverter;
import dev.goldenstack.loot.provider.number.NumberProvider;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a {@code LootFunction} that can change the amount of the ItemStack that is provided.
 */
public class SetCountFunction extends ConditionalLootFunction {

    public static final @NotNull JsonLootConverter<SetCountFunction> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:set_count"), SetCountFunction.class) {
        @Override
        public @NotNull SetCountFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new SetCountFunction(
                    ConditionalLootFunction.deserializeConditions(json, loader),
                    loader.getNumberProviderManager().deserialize(json.get("count"), "count"),
                    JsonHelper.assureBoolean(json.get("add"), "add")
            );
        }

        @Override
        public void serialize(@NotNull SetCountFunction input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            ConditionalLootFunction.serializeConditionalLootFunction(input, result, loader);
            result.add("count", loader.getNumberProviderManager().serialize(input.count));
            result.addProperty("add", input.add);
        }
    };

    private final @NotNull NumberProvider count;
    private final boolean add;

    /**
     * Initialize a SetCountFunction with the provided amount and whether or not the amount will be added instead of set
     */
    public SetCountFunction(@NotNull List<LootCondition> conditions, @NotNull NumberProvider count, boolean add) {
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
     * If {@link #add()} is false, sets the ItemStack's amount to the value of {@link #count()}. Otherwise, adds the
     * value to the ItemStack's count.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        int count = this.count.getInteger(context);
        return itemStack.withAmount(add ? count + itemStack.amount() : count);
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
        return conditions().equals(that.conditions()) && add == that.add && count.equals(that.count);
    }

    @Override
    public int hashCode() {
        return count.hashCode() * 31 + Boolean.hashCode(this.add);
    }
}