package li.cil.tis3d.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Methods to replace things available in MC 1.8.8.
 */
public final class OneEightCompat {
    private static final Random RANDOM = new Random();

    /**
     * All Facings with horizontal axis in order S-W-N-E
     */
    public static final EnumFacing[] HORIZONTALS = new EnumFacing[]{
            EnumFacing.SOUTH,
            EnumFacing.EAST, // Actually WEST, incorrectly named in enum.
            EnumFacing.NORTH,
            EnumFacing.WEST // Actually EAST, incorrectly named in enum.
    };

    // Container.calcRedstone
    public static int calcRedstone(final TileEntity te) {
        return te instanceof IInventory ? Container.calcRedstoneFromInventory((IInventory) te) : 0;
    }

    // Entity.getDistanceSqToCenter
    public static double getDistanceSqToCenter(final Entity entity, final int x, final int y, final int z) {
        final double dx = x + 0.5D - entity.posX;
        final double dy = y + 0.5D - entity.posY;
        final double dz = z + 0.5D - entity.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    // Entity.getHorizontalFacing
    public static EnumFacing getHorizontalFacing(final Entity entity) {
        return getHorizontal(MathHelper.floor_double(entity.rotationYaw * 4 / 360 + 0.5f) & 3);
    }

    // EnumFacing.getHorizontal
    public static EnumFacing getHorizontal(final int index) {
        return HORIZONTALS[MathHelper.abs_int(index % HORIZONTALS.length)];
    }

    // InventoryHelper.dropInventoryItems
    public static void dropInventoryItems(final World world, final int x, final int y, final int z, final IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            final ItemStack stack = inventory.getStackInSlot(i);

            if (stack != null) {
                spawnItemStack(world, x, y, z, stack);
            }
        }
    }

    // InventoryHelper.spawnItemStack
    private static void spawnItemStack(final World world, final double x, final double y, final double z, final ItemStack stack) {
        final float offsetX = RANDOM.nextFloat() * 0.8F + 0.1F;
        final float offsetY = RANDOM.nextFloat() * 0.8F + 0.1F;
        final float offsetZ = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0) {
            int count = RANDOM.nextInt(21) + 10;

            if (count > stack.stackSize) {
                count = stack.stackSize;
            }

            stack.stackSize -= count;
            final EntityItem entity = new EntityItem(world, x + offsetX, y + offsetY, z + offsetZ, new ItemStack(stack.getItem(), count, stack.getItemDamage()));

            if (stack.hasTagCompound()) {
                entity.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
            }

            final float velocity = 0.05F;
            entity.motionX = RANDOM.nextGaussian() * velocity;
            entity.motionY = RANDOM.nextGaussian() * velocity + 0.2;
            entity.motionZ = RANDOM.nextGaussian() * velocity;
            world.spawnEntityInWorld(entity);
        }
    }

    private OneEightCompat() {
    }
}
