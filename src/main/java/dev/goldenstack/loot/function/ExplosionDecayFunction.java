package dev.goldenstack.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.goldenstack.loot.ImmuTables;
import dev.goldenstack.loot.condition.LootCondition;
import dev.goldenstack.loot.context.LootContext;
import dev.goldenstack.loot.context.LootContextParameter;
import dev.goldenstack.loot.json.LootDeserializer;
import dev.goldenstack.loot.json.LootSerializer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Represents a {@code LootFunction} that decreases the count of provided items based on the context's explosion
 * distance and its random instance.
 */
public class ExplosionDecayFunction extends ConditionalLootFunction {
    /**
     * The immutable key for all {@code ExplosionDecayFunction}s
     */
    public static final @NotNull NamespaceID KEY = NamespaceID.from(NamespaceID.MINECRAFT_NAMESPACE, "explosion_decay");

    /**
     * Creates a new ExplosionDecayFunction with the provided conditions
     */
    public ExplosionDecayFunction(@NotNull ImmutableList<LootCondition> conditions) {
        super(conditions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(@NotNull JsonObject object, @NotNull ImmuTables loader) throws JsonParseException {
        super.serialize(object, loader);
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
     * Modifies the ItemStack's count based on the context's explosion radius and its random instance
     */
    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack itemStack, @NotNull LootContext context) {
        @Nullable Float f = context.getParameter(LootContextParameter.EXPLOSION_RADIUS);
        if (f == null || f <= 1) {
            return itemStack;
        }
        Random random = context.findRandom();
        float div = 1.0f / f;

        int count = 0;
        for (int i = 0; i < itemStack.getAmount(); i++) {
            if (random.nextFloat() <= div) {
                count++;
            }
        }
        return itemStack.getAmount() == count ? itemStack : itemStack.withAmount(count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull LootDeserializer<? extends LootSerializer<LootFunction>> getDeserializer() {
        return ExplosionDecayFunction::deserialize;
    }

    @Override
    public String toString() {
        return "ExplosionDecayFunction[conditions=" + conditions() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExplosionDecayFunction that = (ExplosionDecayFunction) o;
        return conditions().equals(that.conditions());
    }

    @Override
    public int hashCode() {
        return conditions().hashCode() * 37;
    }

    /**
     * Static method to deserialize a {@code JsonObject} to an {@code ExplosionDecayFunction}
     */
    public static @NotNull LootFunction deserialize(@NotNull JsonObject json, @NotNull ImmuTables loader) throws JsonParseException {
        return new ExplosionDecayFunction(ConditionalLootFunction.deserializeConditions(json, loader));
    }
}