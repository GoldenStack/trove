package net.goldenstack.loot.util.predicate;

import net.goldenstack.loot.util.Serial;
import net.goldenstack.loot.util.Template;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public record BlockPredicate(@NotNull Map<String, PropertyPredicate> checks) implements Predicate<@NotNull Block> {

    public static final @NotNull BinaryTagSerializer<BlockPredicate> SERIALIZER = Serial.map(PropertyPredicate.SERIALIZER)
            .map(BlockPredicate::new, BlockPredicate::checks)
            .optional(new BlockPredicate(Map.of()));

    public sealed interface PropertyPredicate extends BiPredicate<@NotNull Block, @NotNull String> {

        @NotNull BinaryTagSerializer<PropertyPredicate> SERIALIZER = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull PropertyPredicate value) {
                return switch (value) {
                    case Literal lit -> Literal.SERIALIZER.write(context, lit);
                    case Range range -> Range.SERIALIZER.write(context, range);
                };
            }

            @Override
            public @NotNull PropertyPredicate read(@NotNull Context context, @NotNull BinaryTag tag) {
                return switch (tag) {
                    case CompoundBinaryTag ignored -> Range.SERIALIZER.read(context, tag);
                    default -> Literal.SERIALIZER.read(context, tag);
                };
            }
        };

        record Literal(@NotNull String value) implements PropertyPredicate {

            public static final @NotNull BinaryTagSerializer<Literal> SERIALIZER = BinaryTagSerializer.STRING.map(Literal::new, Literal::value);

            @Override
            public boolean test(@NotNull Block block, @NotNull String key) {
                return value.equals(block.getProperty(key));
            }
        }

        record Range(@Nullable Long min, @Nullable Long max) implements PropertyPredicate {

            public static final @NotNull BinaryTagSerializer<Range> SERIALIZER = Template.template(
                    "min", Serial.LONG.optional(), Range::min,
                    "max", Serial.LONG.optional(), Range::max,
                    Range::new
            );

            @Override
            public boolean test(@NotNull Block block, @NotNull String key) {
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
        }
    }

    public BlockPredicate {
        checks = Map.copyOf(checks);
    }

    @Override
    public boolean test(@NotNull Block block) {
        if (checks.isEmpty()) {
            return true;
        }

        for (var entry : checks.entrySet()) {
            if (!entry.getValue().test(block, entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}
