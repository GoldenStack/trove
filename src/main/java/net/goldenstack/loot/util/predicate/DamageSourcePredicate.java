package net.goldenstack.loot.util.predicate;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface DamageSourcePredicate {

    boolean test(@NotNull Instance world, @NotNull Point pos, @NotNull DamageType type);

}
