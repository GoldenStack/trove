package net.goldenstack.loot.util.predicate;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityPredicate {

    boolean test(@NotNull Instance instance, @Nullable Point pos, @Nullable Entity entity);

}
