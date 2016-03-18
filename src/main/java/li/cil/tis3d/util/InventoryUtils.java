package li.cil.tis3d.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public static EntityItem drop(final World world, final BlockPos pos, final IInventory inventory, final int index, final int count, final EnumFacing towards) {
        final ItemStack stack = inventory.decrStackSize(index, count);
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
    public static EntityItem spawnStackInWorld(final World world, final BlockPos pos, final ItemStack stack, final EnumFacing towards) {
        if (stack == null || stack.stackSize < 1) {
            return null;
        }

        final Random rng = world.rand;

        final double ox = towards.getFrontOffsetX();
        final double oy = towards.getFrontOffsetY();
        final double oz = towards.getFrontOffsetZ();
        final double tx = 0.1 * (rng.nextDouble() - 0.5) + ox * 0.65;
        final double ty = 0.1 * (rng.nextDouble() - 0.5) + oy * 0.75 + (ox + oz) * 0.25;
        final double tz = 0.1 * (rng.nextDouble() - 0.5) + oz * 0.65;
        final double px = pos.getX() + 0.5 + tx;
        final double py = pos.getY() + 0.5 + ty;
        final double pz = pos.getZ() + 0.5 + tz;

        final EntityItem entity = new EntityItem(world, px, py, pz, stack.copy());

        entity.motionX = 0.0125 * (rng.nextDouble() - 0.5) + ox * 0.03;
        entity.motionY = 0.0125 * (rng.nextDouble() - 0.5) + oy * 0.08 + (ox + oz) * 0.03;
        entity.motionZ = 0.0125 * (rng.nextDouble() - 0.5) + oz * 0.03;
        entity.setPickupDelay(15);
        world.spawnEntityInWorld(entity);

        return entity;
    }

    // --------------------------------------------------------------------- //

    private InventoryUtils() {
    }
}
