package li.cil.tis3d.common.block;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.util.InventoryUtils;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Block for the module casings.
 */
public final class BlockCasing extends Block {
    private static final PropertyBool MODULE_X_NEG = PropertyBool.create("xneg");
    private static final PropertyBool MODULE_X_POS = PropertyBool.create("xpos");
    private static final PropertyBool MODULE_Y_NEG = PropertyBool.create("yneg");
    private static final PropertyBool MODULE_Y_POS = PropertyBool.create("ypos");
    private static final PropertyBool MODULE_Z_NEG = PropertyBool.create("zneg");
    private static final PropertyBool MODULE_Z_POS = PropertyBool.create("zpos");

    // --------------------------------------------------------------------- //

    public BlockCasing() {
        super(Material.IRON);
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MODULE_X_NEG, MODULE_X_POS, MODULE_Y_NEG, MODULE_Y_POS, MODULE_Z_NEG, MODULE_Z_POS);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntity tileEntity = WorldUtils.getTileEntityThreadsafe(world, pos);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return super.getActualState(state, world, pos);
        }
        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        return state.
                withProperty(MODULE_X_NEG, casing.getModule(Face.X_NEG) != null).
                withProperty(MODULE_X_POS, casing.getModule(Face.X_POS) != null).
                withProperty(MODULE_Y_NEG, casing.getModule(Face.Y_NEG) != null).
                withProperty(MODULE_Y_POS, casing.getModule(Face.Y_POS) != null).
                withProperty(MODULE_Z_NEG, casing.getModule(Face.Z_NEG) != null).
                withProperty(MODULE_Z_POS, casing.getModule(Face.Z_POS) != null);
    }

    // --------------------------------------------------------------------- //
    // Client

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
        // Allow picking modules installed in the casing.
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            final ItemStack stack = casing.getStackInSlot(target.sideHit.ordinal());
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean isSideSolid(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        // Prevent from torches and the like to be placed on us.
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(final IBlockState state) {
        // Prevent fences from visually connecting.
        return false;
    }

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileEntityCasing();
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (world.isBlockLoaded(pos)) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;
                final ItemStack heldItem = player.getHeldItem(hand);

                // Locking or unlocking the casing or a port?
                if (Items.isKey(heldItem)) {
                    if (!world.isRemote) {
                        if (casing.isLocked()) {
                            casing.unlock(heldItem);
                        } else {
                            if (!player.isSneaking()) {
                                casing.lock(heldItem);
                            } else {
                                final Face face = Face.fromEnumFacing(side);
                                final Vec3d uv = TransformUtil.hitToUV(face, new Vec3d(hitX, hitY, hitZ));
                                final Port port = Port.fromUVQuadrant(uv);

                                casing.setReceivingPipeLocked(face, port, !casing.isReceivingPipeLocked(face, port));
                            }
                        }
                    }
                    return true;
                }

                // Trying to look something up in the manual?
                if (Items.isBookManual(heldItem)) {
                    final ItemStack moduleStack = casing.getStackInSlot(side.ordinal());
                    if (ItemBookManual.tryOpenManual(world, player, ManualAPI.pathFor(moduleStack))) {
                        return true;
                    }
                }

                // Let the module handle the activation.
                final Module module = casing.getModule(Face.fromEnumFacing(side));
                if (module != null && module.onActivate(player, hand, hitX, hitY, hitZ)) {
                    return true;
                }

                // Don't allow changing modules while casing is locked.
                if (casing.isLocked()) {
                    return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
                }

                // Remove old module or install new one.
                final ItemStack oldModule = casing.getStackInSlot(side.ordinal());
                if (!oldModule.isEmpty()) {
                    // Removing a present module from the casing.
                    if (!world.isRemote) {
                        final EntityItem entity = InventoryUtils.drop(world, pos, casing, side.ordinal(), 1, side);
                        if (entity != null) {
                            entity.setNoPickupDelay();
                            entity.onCollideWithPlayer(player);
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                        }
                    }
                    return true;
                } else if (!heldItem.isEmpty()) {
                    // Installing a new module in the casing.
                    if (casing.canInsertItem(side.ordinal(), heldItem, side)) {
                        if (!world.isRemote) {
                            final ItemStack insertedStack;
                            if (player.capabilities.isCreativeMode) {
                                insertedStack = heldItem.copy().splitStack(1);
                            } else {
                                insertedStack = heldItem.splitStack(1);
                            }
                            if (side.getAxis() == EnumFacing.Axis.Y) {
                                final Port orientation = Port.fromEnumFacing(player.getHorizontalFacing());
                                casing.setInventorySlotContents(side.ordinal(), insertedStack, orientation);
                            } else {
                                casing.setInventorySlotContents(side.ordinal(), insertedStack);
                            }
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                        }
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCasing) {
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityCasing) tileentity);
            world.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(world, pos, state);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(final IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(final IBlockState state, final World world, final BlockPos pos) {
        return Container.calcRedstone(world.getTileEntity(pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        final TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileentity;
            final Module module = casing.getModule(Face.fromEnumFacing(side.getOpposite()));
            if (module instanceof Redstone) {
                return ((Redstone) module).getRedstoneOutput();
            }
        }
        return super.getWeakPower(blockState, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            casing.checkNeighbors();
            casing.notifyModulesOfBlockChange(neighborPos);
            casing.markRedstoneDirty();
        }
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos);
    }
}
