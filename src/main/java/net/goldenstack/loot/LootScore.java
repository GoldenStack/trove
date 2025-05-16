package net.goldenstack.loot;

import net.goldenstack.loot.util.RelevantEntity;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A score provider that produces functions that map an objective to a score value.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootScore extends Function<@NotNull LootContext, Function<@NotNull String, @Nullable Integer>> {

    @NotNull Codec<LootScore> CODEC = RelevantEntity.CODEC.<LootScore>transform(Context::new, c -> ((Context) c).name()).orElse(Codec.RegistryTaggedUnion(registries -> {
        class Holder {
            static final @NotNull DynamicRegistry<StructCodec<? extends LootScore>> CODEC = createDefaultRegistry();
        }
        return Holder.CODEC;
    }, LootScore::codec, "type"));

    static @NotNull DynamicRegistry<StructCodec<? extends LootScore>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LootScore>> registry = DynamicRegistry.create("minecraft:loot_scores");
        registry.register("context", Context.CODEC);
        registry.register("fixed", Fixed.CODEC);
        return registry;
    }

    @Override
    @NotNull Function<@NotNull String, @Nullable Integer> apply(@NotNull LootContext context);

    /**
     * @return the codec that can encode this score
     */
    @NotNull StructCodec<? extends LootScore> codec();

    record Context(@NotNull RelevantEntity name) implements LootScore {
        public static final @NotNull StructCodec<Context> CODEC = StructCodec.struct(
                "name", RelevantEntity.CODEC, Context::name,
                Context::new
        );

        @Override
        public @NotNull Function<@NotNull String, @Nullable Integer> apply(@NotNull LootContext context) {
            return objective -> context.vanilla().score(context.require(name.key()), objective);
        }

        @Override
        public @NotNull StructCodec<? extends LootScore> codec() {
            return CODEC;
        }
    }

    record Fixed(@NotNull String name) implements LootScore {
        public static final @NotNull StructCodec<Fixed> CODEC = StructCodec.struct(
                "name", Codec.STRING, Fixed::name,
                Fixed::new
        );

        @Override
        public @NotNull Function<@NotNull String, @Nullable Integer> apply(@NotNull LootContext context) {
            return objective -> context.vanilla().score(name, objective);
        }

        @Override
        public @NotNull StructCodec<? extends LootScore> codec() {
            return CODEC;
        }
    }

}
