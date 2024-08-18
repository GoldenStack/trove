package net.goldenstack.loot.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface LocationPredicate {

    boolean test(@NotNull Instance instance, @NotNull Point point);

}
