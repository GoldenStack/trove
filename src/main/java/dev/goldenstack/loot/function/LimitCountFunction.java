package dev.goldenstack.loot.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.JsonLootConverter;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a {@code LootFunction} that limits the stack size of the provided ItemStack.
 */
public class LimitCountFunction extends ConditionalLootFunction {

    private final NumberRange limiter;

    /**
     * Initialize a LimitCountFunction with the NumberRange as the limiter
     */
    public LimitCountFunction(@NotNull List<LootCondition> conditions, @NotNull NumberRange limiter) {
        super(conditions);
        this.limiter = limiter;
    }

    /**
     * Returns the limiter that is used to limit the count
     */
    public @NotNull NumberRange limiter() {
        return limiter;
    }

    /**
     * Limits the provided ItemStack's amount according to {@link #limiter()}.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        return itemStack.withAmount(this.limiter.limit(context, itemStack.amount()));
    }

    @Override
    public String toString() {
        return "LimitCountFunction[conditions=" + conditions() + ", limiter=" + limiter + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LimitCountFunction that = (LimitCountFunction) o;
        return conditions().equals(that.conditions()) && Objects.equals(limiter, that.limiter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(limiter);
    }

    public static final @NotNull JsonLootConverter<LimitCountFunction> CONVERTER = new JsonLootConverter<>(
            NamespaceID.from("minecraft:limit_count"), LimitCountFunction.class) {
        @Override
        public @NotNull LimitCountFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
            return new LimitCountFunction(
                    ConditionalLootFunction.deserializeConditions(json, loader),
                    NumberRange.deserialize(loader, json.get("limit"), "limit")
            );
        }

        @Override
        public void serialize(@NotNull LimitCountFunction input, @NotNull JsonObject result, @NotNull ImmuTables loader) throws JsonParseException {
            ConditionalLootFunction.serializeConditionalLootFunction(input, result, loader);
            result.add("limit", input.limiter.serialize(loader));
        }
    };
}