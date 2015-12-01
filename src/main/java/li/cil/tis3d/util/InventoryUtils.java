package li.cil.tis3d.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public final class InventoryUtils {
    public static EntityItem drop(final World world, final BlockPos pos, final IInventory inventory, final int index, final int count, final EnumFacing towards) {
        final ItemStack stack = inventory.decrStackSize(index, count);
        return spawnStackInWorld(world, pos, stack, towards);
    }

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
