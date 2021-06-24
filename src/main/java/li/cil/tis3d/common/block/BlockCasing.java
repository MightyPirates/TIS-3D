package li.cil.tis3d.common.block;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.tileentity.TileEntities;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.util.InventoryUtils;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.EnumMap;

/**
 * Block for the module casings.
 */
public final class BlockCasing extends Block {
    public static final BooleanProperty MODULE_X_NEG = BooleanProperty.create("xneg");
    public static final BooleanProperty MODULE_X_POS = BooleanProperty.create("xpos");
    public static final BooleanProperty MODULE_Y_NEG = BooleanProperty.create("yneg");
    public static final BooleanProperty MODULE_Y_POS = BooleanProperty.create("ypos");
    public static final BooleanProperty MODULE_Z_NEG = BooleanProperty.create("zneg");
    public static final BooleanProperty MODULE_Z_POS = BooleanProperty.create("zpos");

    public static final EnumMap<Face, BooleanProperty> FACE_TO_PROPERTY = Util.make(() -> {
        final EnumMap<Face, BooleanProperty> map = new EnumMap<>(Face.class);
        map.put(Face.X_NEG, MODULE_X_NEG);
        map.put(Face.X_POS, MODULE_X_POS);
        map.put(Face.Y_NEG, MODULE_Y_NEG);
        map.put(Face.Y_POS, MODULE_Y_POS);
        map.put(Face.Z_NEG, MODULE_Z_NEG);
        map.put(Face.Z_POS, MODULE_Z_POS);
        return map;
    });

    // --------------------------------------------------------------------- //

    public BlockCasing() {
        super(Properties
            .create(Material.IRON)
            .sound(SoundType.METAL)
            .hardnessAndResistance(1.5f, 6f));

        BlockState defaultState = getStateContainer().getBaseState();
        for (final BooleanProperty value : FACE_TO_PROPERTY.values()) {
            defaultState = defaultState.with(value, false);
        }
        setDefaultState(defaultState);
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        for (final BooleanProperty value : FACE_TO_PROPERTY.values()) {
            builder.add(value);
        }
    }

    // --------------------------------------------------------------------- //
    // Client

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult hit, final IBlockReader world, final BlockPos pos, final PlayerEntity player) {
        // Allow picking modules installed in the casing.
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing && hit instanceof BlockRayTraceResult) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            final BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
            final ItemStack stack = casing.getStackInSlot(blockHit.getFace().ordinal());
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return super.getPickBlock(state, hit, world, pos, player);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return TileEntities.CASING.get().create();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit) {
        if (!WorldUtils.isBlockLoaded(world, pos)) {
            return super.onBlockActivated(state, world, pos, player, hand, hit);
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return super.onBlockActivated(state, world, pos, player, hand, hit);
        }

        final Vector3d localHitPos = hit.getHitVec().subtract(Vector3d.copy(hit.getPos()));
        final Direction side = hit.getFace();
        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final ItemStack heldItem = player.getHeldItem(hand);

        // Locking or unlocking the casing or a port?
        if (Items.is(heldItem, Items.KEY)) {
            if (!world.isRemote()) {
                if (casing.isLocked()) {
                    casing.unlock(heldItem);
                } else {
                    if (!player.isSneaking()) {
                        casing.lock(heldItem);
                    } else {
                        final Face face = Face.fromDirection(side);
                        final Vector3d uv = TransformUtil.hitToUV(face, localHitPos);
                        final Port port = Port.fromUVQuadrant(uv);

                        casing.setReceivingPipeLocked(face, port, !casing.isReceivingPipeLocked(face, port));
                    }
                }
            }
            return ActionResultType.func_233537_a_(world.isRemote());
        }

        // Let the module handle the activation.
        final Module module = casing.getModule(Face.fromDirection(side));
        if (module != null && module.onActivate(player, hand, localHitPos)) {
            return ActionResultType.func_233537_a_(world.isRemote());
        }

        // Don't allow changing modules while casing is locked.
        if (casing.isLocked()) {
            return super.onBlockActivated(state, world, pos, player, hand, hit);
        }

        // Remove old module or install new one.
        final ItemStack oldModule = casing.getStackInSlot(side.ordinal());
        if (!oldModule.isEmpty()) {
            // Removing a present module from the casing.
            if (!world.isRemote()) {
                final ItemEntity entity = InventoryUtils.drop(world, pos, casing, side.ordinal(), 1, side);
                if (entity != null) {
                    entity.setNoPickupDelay();
                    entity.onCollideWithPlayer(player);
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                }
            }
            return ActionResultType.func_233537_a_(world.isRemote());
        } else if (!heldItem.isEmpty()) {
            // Installing a new module in the casing.
            if (casing.canInsertItem(side.ordinal(), heldItem, side)) {
                if (!world.isRemote()) {
                    final ItemStack insertedStack;
                    if (player.abilities.isCreativeMode) {
                        insertedStack = heldItem.copy().split(1);
                    } else {
                        insertedStack = heldItem.split(1);
                    }
                    if (side.getAxis() == Direction.Axis.Y) {
                        final Port orientation = Port.fromDirection(player.getHorizontalFacing());
                        casing.setInventorySlotContents(side.ordinal(), insertedStack, orientation);
                    } else {
                        casing.setInventorySlotContents(side.ordinal(), insertedStack);
                    }
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.2f, 0.8f + world.rand.nextFloat() * 0.1f);
                }
                return ActionResultType.func_233537_a_(world.isRemote());
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (!state.isIn(newState.getBlock())) {
            final TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof TileEntityCasing) {
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) tileentity);
                world.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(final BlockState state, final World world, final BlockPos pos) {
        return Container.calcRedstone(world.getTileEntity(pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(final BlockState blockState, final IBlockReader world, final BlockPos pos, final Direction side) {
        final TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileentity;
            final Module module = casing.getModule(Face.fromDirection(side.getOpposite()));
            if (module instanceof Redstone) {
                return ((Redstone) module).getRedstoneOutput();
            }
        }
        return super.getWeakPower(blockState, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(final BlockState state) {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos fromPos, final boolean isMoving) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            casing.checkNeighbors();
            casing.notifyModulesOfBlockChange(fromPos);
            casing.markRedstoneDirty();
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }
}
