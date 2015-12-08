package li.cil.tis3d.common.block;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.api.module.Rotatable;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Block for the module casings.
 */
public final class BlockCasing extends Block {
    private static final OBJModel.OBJState[] OBJ_STATE_CACHE = new OBJModel.OBJState[1 << Face.VALUES.length];
    private static final List<String> VISIBLE_GROUPS = new ArrayList<>(6);
    private static final String[] FACE_TO_GROUP = new String[]{
            "casing:module.Y_NEG",
            "casing:module.Y_POS",
            "casing:module.Z_NEG",
            "casing:module.Z_POS",
            "casing:module.X_NEG",
            "casing:module.X_POS"
    };

    public BlockCasing() {
        super(Material.iron);
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    public BlockState createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{OBJModel.OBJProperty.instance});
    }

    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final IExtendedBlockState baseState = (IExtendedBlockState) state;
        final TileEntity tileEntity = world.getTileEntity(pos);
        final int mask = packVisibility(tileEntity);
        return baseState.withProperty(OBJModel.OBJProperty.instance, getCachedObjState(mask));
    }

    private OBJModel.OBJState getCachedObjState(final int mask) {
        synchronized (OBJ_STATE_CACHE) {
            if (OBJ_STATE_CACHE[mask] == null) {
                VISIBLE_GROUPS.clear();
                VISIBLE_GROUPS.add("casing:casing");
                for (final Face face : Face.VALUES) {
                    if ((mask & (1 << face.ordinal())) != 0) {
                        VISIBLE_GROUPS.add(FACE_TO_GROUP[face.ordinal()]);
                    }
                }
                OBJ_STATE_CACHE[mask] = new OBJModel.OBJState(VISIBLE_GROUPS, true);
            }
            return OBJ_STATE_CACHE[mask];
        }
    }

    private int packVisibility(final TileEntity tileEntity) {
        int mask = 0;
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            for (final Face face : Face.VALUES) {
                if (casing.getModule(face) != null) {
                    mask |= 1 << face.ordinal();
                }
            }
        }
        return mask;
    }

    // --------------------------------------------------------------------- //
    // Client

    @Override
    public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final BlockPos pos, final EntityPlayer player) {
        // Allow picking modules installed in the casing.
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            final ItemStack stack = casing.getStackInSlot(target.sideHit.ordinal());
            if (stack != null) {
                return stack.copy();
            }
        }
        return super.getPickBlock(target, world, pos, player);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean isSideSolid(final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        // Prevent from torches and the like to be placed on us.
        return false;
    }

    @Override
    public boolean isFullCube() {
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
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (world.isBlockLoaded(pos)) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;

                final Module module = casing.getModule(Face.fromEnumFacing(side));
                if (module != null && module.onActivate(player, hitX, hitY, hitZ)) {
                    return true;
                }

                final ItemStack oldModule = casing.getStackInSlot(side.ordinal());
                if (oldModule != null) {
                    if (!world.isRemote) {
                        final EntityItem entity = InventoryUtils.drop(world, pos, casing, side.ordinal(), 1, side);
                        if (entity != null) {
                            entity.setNoPickupDelay();
                            entity.onCollideWithPlayer(player);
                            world.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "tile.piston.in", 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                        }
                    }
                    return true;
                } else {
                    final ItemStack stack = player.getHeldItem();
                    if (casing.canInsertItem(side.ordinal(), stack, side)) {
                        if (!world.isRemote) {
                            if (player.capabilities.isCreativeMode) {
                                casing.setInventorySlotContents(side.ordinal(), stack.copy().splitStack(1));
                            } else {
                                casing.setInventorySlotContents(side.ordinal(), stack.splitStack(1));
                            }
                            if (side.getAxis() == EnumFacing.Axis.Y) {
                                final Face face = Face.fromEnumFacing(side);
                                final Module newModule = casing.getModule(face);
                                if (newModule instanceof Rotatable) {
                                    final Port orientation = Port.fromEnumFacing(player.getHorizontalFacing());
                                    ((Rotatable) newModule).setFacing(orientation);
                                }
                            }
                            world.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "tile.piston.out", 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                        }
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
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

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(final World world, final BlockPos pos) {
        return Container.calcRedstone(world.getTileEntity(pos));
    }

    @Override
    public int getWeakPower(final IBlockAccess world, final BlockPos pos, final IBlockState state, final EnumFacing side) {
        final TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileentity;
            final Module module = casing.getModule(Face.fromEnumFacing(side.getOpposite()));
            if (module instanceof Redstone) {
                return ((Redstone) module).getRedstoneOutput();
            }
        }
        return super.getWeakPower(world, pos, state, side);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @Override
    public void onNeighborBlockChange(final World world, final BlockPos pos, final IBlockState state, final Block neighborBlock) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            casing.checkNeighbors();
        }
        super.onNeighborBlockChange(world, pos, state, neighborBlock);
    }
}
