package dev.goldenstack.loot.minestom.check;

import dev.goldenstack.loot.context.LootConversionContext;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns true if all of the {@link #checks()} return true for a provided block, and false if not.
 * @param checks the list of checks to apply
 */
public record BlockStateCheck(@NotNull List<SingularCheck> checks) {

    private interface SingularCheck {

        @NotNull String key();

        boolean check(@NotNull Block block);

        @NotNull ConfigurationNode serialize(@NotNull LootConversionContext<ItemStack> context) throws ConfigurateException;

        private static @NotNull SingularCheck deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            if (node.isMap()) {
                return new RangedLongState(
                        String.valueOf(node.key()),
                        node.hasChild("min") ? node.node("min").getLong() : null,
                        node.hasChild("max") ? node.node("max").getLong() :  null
                );
            }
            String string = node.getString();
            if (node.empty() || string == null) {
                throw new ConfigurateException(node, "Expected the provided node to be a map or a string");
            }
            return new IdenticalState(String.valueOf(node.key()), string);
        }

    }

    private record IdenticalState(@NotNull String key, @NotNull String value) implements SingularCheck {
        @Override
        public boolean check(@NotNull Block block) {
            return value.equals(block.getProperty(key));
        }

        @Override
        public @NotNull ConfigurationNode serialize(@NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            return context.loader().createNode().set(value);
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
        public @NotNull ConfigurationNode serialize(@NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
            ConfigurationNode node = context.loader().createNode();
            node.node("min").set(min);
            node.node("max").set(max);
            return node;
        }
    }

    /**
     * @param check the block state check to serialize
     * @param context the (currently unused in this method) context
     * @return a JSON representing the provided check
     * @throws ConfigurateException if the provided check could not be correctly converted to JSON
     */
    public static @NotNull ConfigurationNode serialize(@NotNull BlockStateCheck check, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        ConfigurationNode node = context.loader().createNode();
        for (var sCheck : check.checks()) {
            node.node(sCheck.key()).set(sCheck.serialize(context));
        }
        return node;
    }

    /**
     * @param node the node to try to deserialize
     * @param context the (currently unused in this method) context
     * @return the check that was created from the provided element
     * @throws ConfigurateException if the provided element was not a valid block state check
     */
    public static @NotNull BlockStateCheck deserialize(@NotNull ConfigurationNode node, @NotNull LootConversionContext<ItemStack> context) throws ConfigurateException {
        if (node.empty()) {
            return new BlockStateCheck(List.of());
        }
        List<SingularCheck> checks = new ArrayList<>();
        for (var entry : node.childrenMap().entrySet()) {
            checks.add(SingularCheck.deserialize(entry.getValue(), context));
        }
        return new BlockStateCheck(checks);
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
