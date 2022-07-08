package dev.goldenstack.loot.minestom.check;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.goldenstack.loot.context.LootConversionContext;
import dev.goldenstack.loot.conversion.LootConversionException;
import dev.goldenstack.loot.util.JsonUtils;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Returns true if all of the {@link #checks()} return true for a provided block, and false if not.
 * @param checks the list of checks to apply
 */
public record BlockStateCheck(@NotNull List<SingularCheck> checks) {

    private interface SingularCheck {

        @NotNull String key();

        boolean check(@NotNull Block block);

        @NotNull JsonElement serialize(@NotNull LootConversionContext<ItemStack> context) throws LootConversionException;

        private static @NotNull SingularCheck deserialize(@NotNull JsonElement element, @NotNull String key, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            if (element.isJsonPrimitive()) {
                return new IdenticalState(key, element.getAsString());
            }
            JsonObject object = JsonUtils.assureJsonObject(element, key);
            Number rawMin = JsonUtils.getAsNumber(object.get("min"));
            Number rawMax = JsonUtils.getAsNumber(object.get("max"));
            return new RangedLongState(
                    key,
                    rawMin == null ? null : rawMin.longValue(),
                    rawMax == null ? null : rawMax.longValue()
            );
        }

    }

    private record IdenticalState(@NotNull String key, @NotNull String value) implements SingularCheck {
        @Override
        public boolean check(@NotNull Block block) {
            return value.equals(block.getProperty(key));
        }

        @Override
        public @NotNull JsonElement serialize(@NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            return new JsonPrimitive(value);
        }
    }

    private record RangedLongState(@NotNull String key, @Nullable Long min, @Nullable Long max) implements SingularCheck {
        @Override
        public boolean check(@NotNull Block block) {
            String value = block.getProperty(key);
            if (value == null) {
                return false;
            }
            try {
                long parsedValue = Long.parseLong(value);
                return (min == null || parsedValue >= min) && (max == null || parsedValue <= max);
            } catch (NumberFormatException exception) {
                return false;
            }
        }

        @Override
        public @NotNull JsonElement serialize(@NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
            JsonObject object = new JsonObject();
            if (min != null) {
                object.addProperty("min", min);
            }
            if (max != null) {
                object.addProperty("max", max);
            }
            return object;
        }
    }

    /**
     * @param element the element to try to deserialize
     * @param context the (currently unused in this method) context
     * @return the check that was created from the provided element
     * @throws LootConversionException if the provided element was not a valid block state check
     */
    public static @NotNull BlockStateCheck deserialize(@Nullable JsonElement element, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        if (JsonUtils.isNull(element)) {
            return new BlockStateCheck(List.of());
        }
        JsonObject object = JsonUtils.assureJsonObject(element, null);
        List<SingularCheck> checks = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            checks.add(SingularCheck.deserialize(entry.getValue(), entry.getKey(), context));
        }
        return new BlockStateCheck(checks);
    }

    /**
     * @param check the block state check to serialize
     * @param context the (currently unused in this method) context
     * @return a JSON representing the provided check
     * @throws LootConversionException if the provided check could not be correctly converted to JSON
     */
    public static @NotNull JsonElement serialize(@NotNull BlockStateCheck check, @NotNull LootConversionContext<ItemStack> context) throws LootConversionException {
        if (check.checks().isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject object = new JsonObject();
        for (SingularCheck singularCheck : check.checks()) {
            object.add(singularCheck.key(), singularCheck.serialize(context));
        }
        return object;
    }

    public BlockStateCheck {
        checks = List.copyOf(checks);
    }

    /**
     * Sequentially applies all the {@link #checks()} to any provided blocks and returns false if any of them, well,
     * are false.<br>
     * Note: If this instance has no checks, the result will always be true.
     * @param block the block to test
     * @return true if every check approved the provided block
     */
    public boolean test(@NotNull Block block) {
        if (checks.isEmpty()) {
            return true;
        }
        for (SingularCheck check : checks) {
            if (!check.check(block)) {
                return false;
            }
        }
        return true;
    }
}
