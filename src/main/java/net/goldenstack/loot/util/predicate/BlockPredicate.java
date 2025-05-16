package net.goldenstack.loot.util.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public record BlockPredicate(@NotNull Map<String, PropertyPredicate> checks) implements Predicate<@NotNull Block> {

    public static final @NotNull Codec<BlockPredicate> CODEC = Codec.STRING.mapValue(PropertyPredicate.CODEC)
            .transform(BlockPredicate::new, BlockPredicate::checks)
            .optional(new BlockPredicate(Map.of()));

    public sealed interface PropertyPredicate extends BiPredicate<@NotNull Block, @NotNull String> {

        @NotNull Codec<PropertyPredicate> CODEC = Literal.CODEC.transform(a->(PropertyPredicate)a,a->(Literal)a)
                .orElse(Range.CODEC.transform(a->a, a->(Range)a));

        record Literal(@NotNull String value) implements PropertyPredicate {

            public static final @NotNull Codec<Literal> CODEC = Codec.STRING.transform(Literal::new, Literal::value);

            @Override
            public boolean test(@NotNull Block block, @NotNull String key) {
                return value.equals(block.getProperty(key));
            }
        }

        record Range(@Nullable Long min, @Nullable Long max) implements PropertyPredicate {

            public static final @NotNull StructCodec<Range> CODEC = StructCodec.struct(
                    "min", Codec.LONG.optional(), Range::min,
                    "max", Codec.LONG.optional(), Range::max,
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
