package net.goldenstack.loot.util.predicate;

import net.goldenstack.loot.util.Template;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public interface EntityPredicate {

    @SuppressWarnings("UnstableApiUsage")
    @NotNull AtomicReference<BinaryTagSerializer<EntityPredicate>> SERIALIZER = new AtomicReference<>(Template.template(() -> (instance, pos, entity) -> false));

    boolean test(@NotNull Instance instance, @Nullable Point pos, @Nullable Entity entity);

}
