package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.LootTableLoader;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import dev.goldenstack.loot.util.NumberRange;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a {@code LootFunction} that limits the stack size of the provided ItemStack.
 */
public class LimitCountFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code LimitCountFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "limit_count");

    private final NumberRange limiter;

    /**
     * Initialize a LimitCountFunction with the NumberRange as the limiter
     */
    public LimitCountFunction(@NotNull ImmutableList<LootCondition> conditions, @NotNull NumberRange limiter){
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
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull LootTableLoader loader) throws JsonParseException {
        super.serialize(object, loader);
        object.add("limit", loader.serializeNumberRange(this.limiter));
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
     * Limits the provided ItemStack's amount according to {@link #limiter()}.
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        return itemStack.withAmount(this.limiter.limit(context, itemStack.getAmount()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return LimitCountFunction::deserialize;
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

    /**
     * Static method to deserialize a {@code JsonObject} to a {@code LimitCountFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull LootTableLoader loader) throws JsonParseException {
        ImmutableList<LootCondition> list = ConditionalLootFunction.deserializeConditions(json, loader);
        NumberRange range = loader.deserializeNumberRange(json.get("limit"), "limit");
        return new LimitCountFunction(list, range);
    }
}