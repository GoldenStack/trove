package net.goldenstack.loot;

import net.goldenstack.loot.util.RelevantEntity;
import net.goldenstack.loot.util.Template;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A score provider that produces functions that map an objective to a score value.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootScore extends Function<@NotNull LootContext, Function<@NotNull String, @Nullable Integer>> {

    @NotNull BinaryTagSerializer<LootScore> SERIALIZER = Template.compoundSplit(
            RelevantEntity.SERIALIZER.map(Context::new, Context::entity),
            Template.registry("type",
                    Template.entry("fixed", Fixed.class, Fixed.SERIALIZER),
                    Template.entry("context", Context.class, Context.SERIALIZER)
            )
    );

    @Override
    @NotNull Function<@NotNull String, @Nullable Integer> apply(@NotNull LootContext context);

    record Context(@NotNull RelevantEntity entity) implements LootScore {

        public static final @NotNull BinaryTagSerializer<Context> SERIALIZER = Template.template(
                "name", RelevantEntity.SERIALIZER, Context::entity,
                Context::new
        );

        @Override
        public @NotNull Function<@NotNull String, @Nullable Integer> apply(@NotNull LootContext context) {
            return objective -> context.require(LootContext.VANILLA_INTERFACE).getScore(context.require(entity.key()), objective);
        }
    }

    record Fixed(@NotNull String name) implements LootScore {

        public static final @NotNull BinaryTagSerializer<Fixed> SERIALIZER = Template.template(
                "name", BinaryTagSerializer.STRING, Fixed::name,
                Fixed::new
        );

        @Override
        public @NotNull Function<@NotNull String, @Nullable Integer> apply(@NotNull LootContext context) {
            return objective -> context.require(LootContext.VANILLA_INTERFACE).getScore(name, objective);
        }
    }

}
