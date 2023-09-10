package dev.goldenstack.loot.minestom.util;

import dev.goldenstack.loot.generation.LootProcessor;
import dev.goldenstack.loot.minestom.context.LootContextKeys;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Contains standard Minecraft drop processors.
 */
public class LootProcessors {

    /**
     * Supports adding items to inventories.
     */
    public static @NotNull LootProcessor.Filtered INVENTORY = LootProcessor.typed(ItemStack.class, (context, item) -> {
        var inventory = context.assure(LootContextKeys.INVENTORY);

        int[] slots = IntStream.range(0, inventory.getSize()).filter(i -> inventory.getItemStack(i).isAir()).toArray();

        if (slots.length == 0) return;

        var index = context.random().nextInt(0, slots.length);
        inventory.setItemStack(slots[index], item);
    });

    /**
     * Drops items with random velocities at the origin. This can be used for entity drops.
     */
    public static @NotNull LootProcessor.Filtered ORIGIN = LootProcessor.typed(ItemStack.class, (context, item) ->
            dropItemRandomly(context.assure(LootContextKeys.WORLD), context.assure(LootContextKeys.ORIGIN), new ItemEntity(item))
    );

    /**
     * Drops items with random velocities and offsets at the origin.
     */
    public static @NotNull LootProcessor.Filtered BLOCK = LootProcessor.typed(ItemStack.class, (context, item) -> {
        var rng = ThreadLocalRandom.current();
        var entity = new ItemEntity(item);

        // Spawn at a random place near the center of the block
        var itemPos = context.assure(LootContextKeys.ORIGIN).add(
                rng.nextDouble(0.25, 0.75),
                rng.nextDouble(0.25, 0.75) - entity.getBoundingBox().height() / 2,
                rng.nextDouble(0.25, 0.75)
        );

        dropItemRandomly(context.assure(LootContextKeys.WORLD), itemPos, entity);
    });

    /**
     * Drops an item in the provided instance and at the given position, with a random velocity.<br>
     * Note: This will look like it has too much velocity and slide across the floor too much, as, apparently, Minestom
     * item entiy frictio and drag should be higher.
     * @param instance the instance to drop in
     * @param dropPos the position to drop at
     * @param entity the item entity to drop
     */
    public static void dropItemRandomly(@NotNull Instance instance, @NotNull Point dropPos, @NotNull ItemEntity entity) {
        var rng = ThreadLocalRandom.current();

        // 500ms pickup and merge delay for items dropped
        entity.setPickupDelay(Duration.of(500, ChronoUnit.MILLIS));

        // Generate a random velocity to add
        var velocity = new Vec(
                rng.nextDouble(-0.1, 0.1),
                0.2,
                rng.nextDouble(-0.1, 0.1)
        ).mul(20);

        // Add everything
        entity.setInstance(instance, dropPos);
        entity.setVelocity(velocity);
    }

}
