package net.goldenstack.loot.util.predicate;

import net.goldenstack.loot.util.Template;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public interface LocationPredicate {

    @SuppressWarnings("UnstableApiUsage")
    @NotNull AtomicReference<BinaryTagSerializer<LocationPredicate>> SERIALIZER = new AtomicReference<>(Template.template(() -> (instance, point) -> false));

    boolean test(@NotNull Instance instance, @NotNull Point point);

}
