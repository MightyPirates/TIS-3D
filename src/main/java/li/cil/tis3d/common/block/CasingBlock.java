package li.cil.tis3d.common.block;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.ModuleWithRedstone;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import li.cil.tis3d.common.tileentity.TileEntities;
import li.cil.tis3d.util.InventoryUtils;
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
public final class CasingBlock extends Block {
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

    public CasingBlock() {
        super(Properties
            .of(Material.METAL)
            .sound(SoundType.METAL)
            .strength(1.5f, 6f));

        BlockState defaultState = getStateDefinition().any();
        for (final BooleanProperty value : FACE_TO_PROPERTY.values()) {
            defaultState = defaultState.setValue(value, false);
        }
        registerDefaultState(defaultState);
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        for (final BooleanProperty value : FACE_TO_PROPERTY.values()) {
            builder.add(value);
        }
    }

    // --------------------------------------------------------------------- //
    // Client

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult hit, final IBlockReader world, final BlockPos pos, final PlayerEntity player) {
        // Allow picking modules installed in the casing.
        final TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof CasingTileEntity && hit instanceof BlockRayTraceResult) {
            final CasingTileEntity casing = (CasingTileEntity) tileEntity;
            final BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
            final ItemStack stack = casing.getItem(blockHit.getDirection().ordinal());
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
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit) {
        final TileEntity tileEntity = world.getBlockEntity(pos);
        if (!(tileEntity instanceof CasingTileEntity)) {
            return super.use(state, world, pos, player, hand, hit);
        }

        final BlockPos hitPos = hit.getBlockPos();
        final Vector3d localHitPos = hit.getLocation().subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ());
        final Direction side = hit.getDirection();
        final CasingTileEntity casing = (CasingTileEntity) tileEntity;
        final ItemStack heldItem = player.getItemInHand(hand);

        // Locking or unlocking the casing or a port?
        if (Items.is(heldItem, Items.KEY)) {
            if (!world.isClientSide()) {
                if (casing.isLocked()) {
                    casing.unlock(heldItem);
                } else {
                    if (!player.isShiftKeyDown()) {
                        casing.lock(heldItem);
                    } else {
                        final Face face = Face.fromDirection(side);
                        final Vector3d uv = TransformUtil.hitToUV(face, localHitPos);
                        final Port port = Port.fromUVQuadrant(uv);

                        casing.setReceivingPipeLocked(face, port, !casing.isReceivingPipeLocked(face, port));
                    }
                }
            }
            return ActionResultType.sidedSuccess(world.isClientSide());
        }

        // Let the module handle the activation.
        final Module module = casing.getModule(Face.fromDirection(side));
        if (module != null && module.onActivate(player, hand, localHitPos)) {
            return ActionResultType.sidedSuccess(world.isClientSide());
        }

        // Don't allow changing modules while casing is locked.
        if (casing.isLocked()) {
            return super.use(state, world, pos, player, hand, hit);
        }

        // Remove old module or install new one.
        final ItemStack oldModule = casing.getItem(side.ordinal());
        if (!oldModule.isEmpty()) {
            // Removing a present module from the casing.
            if (!world.isClientSide()) {
                final ItemEntity entity = InventoryUtils.drop(world, pos, casing, side.ordinal(), 1, side);
                if (entity != null) {
                    entity.setNoPickUpDelay();
                    entity.playerTouch(player);
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PISTON_CONTRACT, SoundCategory.BLOCKS, 0.2f, 0.8f + world.random.nextFloat() * 0.1f);
                }
            }
            return ActionResultType.sidedSuccess(world.isClientSide());
        } else if (!heldItem.isEmpty()) {
            // Installing a new module in the casing.
            if (casing.canPlaceItemThroughFace(side.ordinal(), heldItem, side)) {
                if (!world.isClientSide()) {
                    final ItemStack insertedStack;
                    if (player.abilities.instabuild) {
                        insertedStack = heldItem.copy().split(1);
                    } else {
                        insertedStack = heldItem.split(1);
                    }
                    if (side.getAxis() == Direction.Axis.Y) {
                        final Port orientation = Port.fromDirection(player.getDirection());
                        casing.setInventorySlotContents(side.ordinal(), insertedStack, orientation);
                    } else {
                        casing.setItem(side.ordinal(), insertedStack);
                    }
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PISTON_EXTEND, SoundCategory.BLOCKS, 0.2f, 0.8f + world.random.nextFloat() * 0.1f);
                }
                return ActionResultType.sidedSuccess(world.isClientSide());
            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            final TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof CasingTileEntity) {
                InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(final BlockState state, final World world, final BlockPos pos) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(final BlockState blockState, final IBlockReader world, final BlockPos pos, final Direction side) {
        final TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof CasingTileEntity) {
            final CasingTileEntity casing = (CasingTileEntity) tileentity;
            final Module module = casing.getModule(Face.fromDirection(side.getOpposite()));
            if (module instanceof ModuleWithRedstone) {
                return ((ModuleWithRedstone) module).getRedstoneOutput();
            }
        }
        return super.getSignal(blockState, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos fromPos, final boolean isMoving) {
        final TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof CasingTileEntity) {
            final CasingTileEntity casing = (CasingTileEntity) tileEntity;
            casing.checkNeighbors();
            casing.notifyModulesOfBlockChange(fromPos);
            casing.markRedstoneDirty();
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }
}
