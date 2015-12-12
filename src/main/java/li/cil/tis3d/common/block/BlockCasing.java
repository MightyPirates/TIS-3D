package li.cil.tis3d.common.block;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.api.module.Rotatable;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.util.InventoryUtils;
import li.cil.tis3d.util.OneEightCompat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Block for the module casings.
 */
public final class BlockCasing extends Block {
    public BlockCasing() {
        super(Material.iron);
    }

    // --------------------------------------------------------------------- //
    // Client

    @Override
    public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final int x, final int y, final int z, final EntityPlayer player) {
        // Allow picking modules installed in the casing.
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            final ItemStack stack = casing.getStackInSlot(target.sideHit);
            if (stack != null) {
                return stack.copy();
            }
        }
        return super.getPickBlock(target, world, x, y, z, player);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean isBlockSolid(final IBlockAccess world, final int x, final int y, final int z, final int side) {
        // Prevent from torches and the like to be placed on us.
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        // Prevent fences from visually connecting.
        return false;
    }

    @Override
    public int getRenderType() {
        return TIS3D.proxy.getCasingRenderId();
    }

    @Override
    public boolean hasTileEntity(final int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final int metadata) {
        return new TileEntityCasing();
    }

    @Override
    public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
        if (world.blockExists(x, y, z)) {
            final EnumFacing facing = EnumFacing.getFront(side);
            final TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;

                final ItemStack stack = player.getHeldItem();
                if (ItemBookManual.isBookManual(stack)) {
                    final ItemStack moduleStack = casing.getStackInSlot(side);
                    if (ItemBookManual.tryOpenManual(world, player, ManualAPI.pathFor(moduleStack))) {
                        return true;
                    }
                }

                final Module module = casing.getModule(Face.fromEnumFacing(facing));
                if (module != null && module.onActivate(player, hitX, hitY, hitZ)) {
                    return true;
                }

                final ItemStack oldModule = casing.getStackInSlot(side);
                if (oldModule != null) {
                    if (!world.isRemote) {
                        final EntityItem entity = InventoryUtils.drop(world, x, y, z, casing, side, 1, facing);
                        if (entity != null) {
                            entity.delayBeforeCanPickup = 0;
                            entity.onCollideWithPlayer(player);
                            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "tile.piston.in", 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                        }
                    }
                    return true;
                } else {
                    if (casing.canInsertItem(side, stack, side)) {
                        if (!world.isRemote) {
                            if (player.capabilities.isCreativeMode) {
                                casing.setInventorySlotContents(side, stack.copy().splitStack(1));
                            } else {
                                casing.setInventorySlotContents(side, stack.splitStack(1));
                            }
                            if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
                                final Face face = Face.fromEnumFacing(facing);
                                final Module newModule = casing.getModule(face);
                                if (newModule instanceof Rotatable) {
                                    final Port orientation = Port.fromEnumFacing(OneEightCompat.getHorizontalFacing(player));
                                    ((Rotatable) newModule).setFacing(orientation);
                                }
                            }
                            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "tile.piston.out", 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                        }
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(final World world, final int x, final int y, final int z, final Block block, final int metadata) {
        final TileEntity tileentity = world.getTileEntity(x, y, z);
        if (tileentity instanceof TileEntityCasing) {
            OneEightCompat.dropInventoryItems(world, x, y, z, (TileEntityCasing) tileentity);
            world.func_147453_f(x, y, z, this);
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(final World world, final int x, final int y, final int z, final int side) {
        return OneEightCompat.calcRedstone(world.getTileEntity(x, y, z));
    }

    @Override
    public int isProvidingWeakPower(final IBlockAccess world, final int x, final int y, final int z, final int side) {
        final TileEntity tileentity = world.getTileEntity(x, y, z);
        if (tileentity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileentity;
            final Module module = casing.getModule(Face.fromEnumFacing(EnumFacing.getFront(side)).getOpposite());
            if (module instanceof Redstone) {
                return ((Redstone) module).getRedstoneOutput();
            }
        }
        return super.isProvidingWeakPower(world, x, y, z, side);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @Override
    public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final Block neighborBlock) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            casing.checkNeighbors();
        }
        super.onNeighborBlockChange(world, x, y, z, neighborBlock);
    }
}
