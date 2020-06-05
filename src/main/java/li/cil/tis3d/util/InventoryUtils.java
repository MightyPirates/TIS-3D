package li.cil.tis3d.util;

import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Utility class for interacting with inventories.
 */
public final class InventoryUtils {
    /**
     * Drop some items from an inventory into the world.
     *
     * @param world     the world to drop the item into.
     * @param pos       the position in the world to drop the item at.
     * @param inventory the inventory to drop the item from.
     * @param index     the slot of the inventory to drop the item from.
     * @param count     the number of items to drop from the stack in that slot.
     * @param towards   the direction in which to drop the item.
     * @return the entity representing the dropped item stack, or <tt>null</tt> if the stack was null or empty.
     */
    @Nullable
    public static ItemEntity drop(final World world, final BlockPos pos, final Inventory inventory, final int index, final int count, final Direction towards) {
        final ItemStack stack = inventory.removeStack(index, count);
        return spawnStackInWorld(world, pos, stack, towards);
    }

    /**
     * Spawns an item stack in the world.
     *
     * @param world   the world to spawn the item stack in.
     * @param pos     the position to spawn the item stack at.
     * @param stack   the item stack to spawn in the world.
     * @param towards the direction in which to drop the item.
     * @return the entity representing the dropped item stack, or <tt>null</tt> if the stack was null or empty.
     */
    @Nullable
    public static ItemEntity spawnStackInWorld(final World world, final BlockPos pos, final ItemStack stack, final Direction towards) {
        if (stack.isEmpty()) {
            return null;
        }

        final Random rng = world.random;

        final double ox = towards.getOffsetX();
        final double oy = towards.getOffsetY();
        final double oz = towards.getOffsetZ();
        final double tx = 0.1 * (rng.nextDouble() - 0.5) + ox * 0.65;
        final double ty = 0.1 * (rng.nextDouble() - 0.5) + oy * 0.75 + (ox + oz) * 0.25;
        final double tz = 0.1 * (rng.nextDouble() - 0.5) + oz * 0.65;
        final double px = pos.getX() + 0.5 + tx;
        final double py = pos.getY() + 0.5 + ty;
        final double pz = pos.getZ() + 0.5 + tz;

        final ItemEntity entity = new ItemEntity(world, px, py, pz, stack.copy());

        entity.setVelocity(new Vec3d(
            0.0125 * (rng.nextDouble() - 0.5) + ox * 0.03,
            0.0125 * (rng.nextDouble() - 0.5) + oy * 0.08 + (ox + oz) * 0.03,
            0.0125 * (rng.nextDouble() - 0.5) + oz * 0.03
        ));
        entity.setPickupDelay(15);
        world.spawnEntity(entity);

        return entity;
    }

    // --------------------------------------------------------------------- //

    private InventoryUtils() {
    }
}
